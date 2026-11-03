/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.image.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.signtelecom.datalabelbe.domain.image.exception.ImageErrorCode;
import com.signtelecom.datalabelbe.global.exception.CustomException;
import com.signtelecom.datalabelbe.global.exception.ErrorGuard;

import lombok.extern.slf4j.Slf4j;

/**
 * 이미지 파일을 로컬 저장소에 기록하고 삭제하는 컴포넌트
 *
 * <p>파일은 batch/{batchId}/ 아래에 배치 단위로 모아 저장. 저장 파일명은 UUID로 생성하므로 원본 파일명이 겹쳐도 충돌하지 않음
 *
 * <p>클라이언트 검증은 우회될 수 있으므로 용량({@code app.file.max-size-bytes})과 포맷을 서버에서 다시 확인. 허용 포맷(JPEG, PNG) 여부는
 * 클라이언트가 보낸 Content-Type이 아니라 실제로 디코딩된 이미지 포맷으로 판별
 */
@Slf4j
@Component
public class ImageStorageService {

  /** 디코딩된 포맷 이름 → 허용 MIME 타입 */
  private static final Map<String, String> ALLOWED_FORMATS =
      Map.of("jpeg", "image/jpeg", "jpg", "image/jpeg", "png", "image/png");

  /** MIME 타입 → 저장 파일 확장자 */
  private static final Map<String, String> EXTENSIONS =
      Map.of("image/jpeg", "jpg", "image/png", "png");

  /** 배치별 이미지 디렉터리의 접두 경로 */
  private static final String BATCH_DIR_PREFIX = "batch";

  /** 배치 디렉터리 안의 썸네일 캐시 디렉터리 이름 */
  private static final String THUMBNAIL_DIR = "thumb";

  /** 캐시 파일이 무한정 늘어나지 않도록 요청 가능한 너비를 제한 */
  private static final int MIN_THUMBNAIL_WIDTH = 16;

  private static final int MAX_THUMBNAIL_WIDTH = 2048;

  private final Path root;
  private final long maxSizeBytes;

  public ImageStorageService(
      @Value("${app.file.upload-dir}") String uploadDir,
      @Value("${app.file.max-size-bytes}") long maxSizeBytes) {
    this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    this.maxSizeBytes = maxSizeBytes;
  }

  /**
   * 업로드된 파일을 검증한 뒤 배치 디렉터리에 기록
   *
   * @param batchId 파일이 속할 배치 식별자
   * @param file 업로드된 이미지 파일
   * @return 저장된 파일의 메타데이터
   * @throws CustomException 빈 파일, 용량 초과, 허용되지 않는 포맷, 손상된 이미지이거나 파일 기록에 실패한 경우
   */
  public StoredImage store(Long batchId, MultipartFile file) {

    ErrorGuard.throwIf(file == null || file.isEmpty(), ImageErrorCode.EMPTY_FILE);

    // 클라이언트 검증은 우회될 수 있으므로 용량과 포맷을 서버에서 다시 확인
    if (file.getSize() > maxSizeBytes) {
      log.info(
          "용량 제한 초과 - filename: {}, size: {}bytes, limit: {}bytes",
          file.getOriginalFilename(),
          file.getSize(),
          maxSizeBytes);
      ErrorGuard.fail(ImageErrorCode.FILE_TOO_LARGE);
    }

    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      log.error("업로드 파일 읽기 실패 - filename: {}", file.getOriginalFilename(), e);
      return ErrorGuard.fail(ImageErrorCode.IMAGE_SAVE_FAILED, e);
    }

    ImageMetadata metadata = readMetadata(bytes);
    String relativePath = generateRelativePath(batchId, metadata.contentType());
    Path target = root.resolve(relativePath).normalize();

    try {
      Files.createDirectories(target.getParent());
      Files.write(target, bytes);
    } catch (IOException e) {
      log.error("이미지 저장 실패 - storagePath: {}", relativePath, e);
      return ErrorGuard.fail(ImageErrorCode.IMAGE_SAVE_FAILED, e);
    }

    log.info(
        "이미지 저장 완료 - storagePath: {}, contentType: {}, size: {}bytes",
        relativePath,
        metadata.contentType(),
        bytes.length);

    return new StoredImage(
        relativePath, metadata.contentType(), bytes.length, metadata.width(), metadata.height());
  }

  /**
   * 저장소에서 파일을 삭제
   *
   * <p>DB 레코드 정리를 막지 않기 위해, 파일이 이미 없거나 삭제에 실패해도 예외를 던지지 않고 경고만 남김
   *
   * @param storagePath 저장소 루트 기준 상대 경로
   */
  public void delete(String storagePath) {

    Path target = root.resolve(storagePath).normalize();

    if (!target.startsWith(root)) {
      log.warn("저장소 밖의 경로 삭제 시도를 차단 - storagePath: {}", storagePath);
      return;
    }

    try {
      boolean deleted = Files.deleteIfExists(target);
      log.info("이미지 파일 삭제 - storagePath: {}, 실제 삭제 여부: {}", storagePath, deleted);
    } catch (IOException e) {
      log.warn("이미지 파일 삭제 실패 - storagePath: {}", storagePath, e);
    }
  }

  /**
   * 배치 디렉터리를 하위 파일까지 통째로 삭제
   *
   * <p>DB 레코드 정리를 막지 않기 위해, 디렉터리가 이미 없거나 삭제에 실패해도 예외를 던지지 않고 경고만 남김
   *
   * @param batchId 삭제할 배치 식별자
   */
  public void deleteBatchDirectory(Long batchId) {

    Path target = root.resolve("%s/%d".formatted(BATCH_DIR_PREFIX, batchId)).normalize();

    if (!target.startsWith(root)) {
      log.warn("저장소 밖의 경로 삭제 시도를 차단 - batchId: {}", batchId);
      return;
    }

    if (!Files.exists(target)) {
      log.info("삭제할 배치 디렉터리가 없음 - batchId: {}", batchId);
      return;
    }

    // 깊은 경로부터 지워야 디렉터리가 비워진 뒤 삭제됨
    try (Stream<Path> paths = Files.walk(target)) {
      paths
          .sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                } catch (IOException e) {
                  log.warn("배치 디렉터리 내 파일 삭제 실패 - path: {}", path, e);
                }
              });
      log.info("배치 디렉터리 삭제 - batchId: {}", batchId);
    } catch (IOException e) {
      log.warn("배치 디렉터리 삭제 실패 - batchId: {}", batchId, e);
    }
  }

  /**
   * 저장소에서 파일을 읽어 리소스로 반환
   *
   * <p>DB 레코드는 있지만 실제 파일이 없는 경우(외부에서 파일만 삭제된 경우)에는 예외를 던짐. 호출부에서 404로 응답
   *
   * @param storagePath 저장소 루트 기준 상대 경로
   * @return 파일 리소스
   * @throws CustomException 저장소 밖을 가리키거나 파일이 존재하지 않는 경우
   */
  public Resource load(String storagePath) {

    Path target = root.resolve(storagePath).normalize();

    if (!target.startsWith(root)) {
      log.warn("저장소 밖의 경로 조회 시도를 차단 - storagePath: {}", storagePath);
      ErrorGuard.fail(ImageErrorCode.IMAGE_FILE_NOT_FOUND);
    }

    if (!Files.isRegularFile(target)) {
      log.warn("DB 레코드는 있으나 파일이 없음(고아 레코드) - storagePath: {}", storagePath);
      ErrorGuard.fail(ImageErrorCode.IMAGE_FILE_NOT_FOUND);
    }

    return new FileSystemResource(target);
  }

  /**
   * 썸네일을 반환. 없으면 만들어 캐시한 뒤 반환
   *
   * <p>비율을 유지한 채 너비를 맞추고 JPEG 로 인코딩. 원본보다 큰 너비를 요청하면 확대하지 않고 원본 크기로 둠
   *
   * <p>결과는 {@code batch/{batchId}/thumb/{w}/{uuid}.jpg} 에 캐시. 배치 디렉터리 안에 두면 배치를 지울 때 썸네일도 함께 정리됨
   *
   * @param storagePath 원본의 저장소 루트 기준 상대 경로
   * @param width 목표 너비 (px)
   * @return 썸네일 파일 리소스
   * @throws CustomException 너비가 허용 범위를 벗어나거나, 원본이 없거나, 생성에 실패한 경우
   */
  public Resource loadThumbnail(String storagePath, int width) {

    if (width < MIN_THUMBNAIL_WIDTH || width > MAX_THUMBNAIL_WIDTH) {
      log.info("허용되지 않는 썸네일 너비 - width: {}", width);
      ErrorGuard.fail(ImageErrorCode.INVALID_THUMBNAIL_WIDTH);
    }

    Path cached = thumbnailPath(storagePath, width);

    if (Files.isRegularFile(cached)) {
      return new FileSystemResource(cached);
    }

    Path source = root.resolve(storagePath).normalize();

    if (!source.startsWith(root) || !Files.isRegularFile(source)) {
      log.warn("썸네일 원본 없음 - storagePath: {}", storagePath);
      ErrorGuard.fail(ImageErrorCode.IMAGE_FILE_NOT_FOUND);
    }

    try {
      BufferedImage original = ImageIO.read(source.toFile());

      if (original == null) {
        log.warn("썸네일 원본 디코딩 실패 - storagePath: {}", storagePath);
        ErrorGuard.fail(ImageErrorCode.INVALID_IMAGE);
      }

      BufferedImage resized = resize(original, width);

      Files.createDirectories(cached.getParent());
      if (!ImageIO.write(resized, "jpg", cached.toFile())) {
        log.error("JPEG 인코더를 찾을 수 없음 - storagePath: {}", storagePath);
        ErrorGuard.fail(ImageErrorCode.THUMBNAIL_FAILED);
      }

      log.info(
          "썸네일 생성 - storagePath: {}, {}x{} -> {}x{}",
          storagePath,
          original.getWidth(),
          original.getHeight(),
          resized.getWidth(),
          resized.getHeight());

      return new FileSystemResource(cached);
    } catch (IOException e) {
      log.error("썸네일 생성 실패 - storagePath: {}", storagePath, e);
      return ErrorGuard.fail(ImageErrorCode.THUMBNAIL_FAILED, e);
    }
  }

  /**
   * 이미지 하나의 캐시된 썸네일을 모두 삭제
   *
   * <p>이미지 단건 삭제 시 호출. 배치 단위 삭제는 디렉터리를 통째로 지우므로 따로 부를 필요가 없음
   *
   * @param storagePath 원본의 저장소 루트 기준 상대 경로
   */
  public void deleteThumbnails(String storagePath) {

    Path thumbRoot = root.resolve(thumbnailDir(storagePath)).normalize();

    if (!thumbRoot.startsWith(root) || !Files.isDirectory(thumbRoot)) {
      return;
    }

    String basename = thumbnailFilename(storagePath);

    try (Stream<Path> widths = Files.list(thumbRoot)) {
      widths
          .map(dir -> dir.resolve(basename))
          .forEach(
              path -> {
                try {
                  if (Files.deleteIfExists(path)) {
                    log.info("썸네일 삭제 - path: {}", root.relativize(path));
                  }
                } catch (IOException e) {
                  log.warn("썸네일 삭제 실패 - path: {}", path, e);
                }
              });
    } catch (IOException e) {
      log.warn("썸네일 디렉터리 조회 실패 - storagePath: {}", storagePath, e);
    }
  }

  /** 비율을 유지하며 너비를 맞춤. 알파 채널은 흰 배경에 합성해 JPEG 로 쓸 수 있게 함 */
  private BufferedImage resize(BufferedImage original, int width) {

    // 원본보다 크게 요청하면 확대하지 않음. 화질만 나빠지고 용량은 커짐
    int targetWidth = Math.min(width, original.getWidth());
    int targetHeight =
        Math.max(1, Math.round(original.getHeight() * (float) targetWidth / original.getWidth()));

    BufferedImage resized =
        new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = resized.createGraphics();

    try {
      graphics.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      // JPEG 는 투명도를 못 담으므로 흰 배경을 먼저 깖. 안 깔면 투명 영역이 검게 나옴
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, targetWidth, targetHeight);
      graphics.drawImage(original, 0, 0, targetWidth, targetHeight, null);
    } finally {
      graphics.dispose();
    }

    return resized;
  }

  /** batch/{batchId}/thumb/{w}/{uuid}.jpg */
  private Path thumbnailPath(String storagePath, int width) {
    return root.resolve(
            "%s/%d/%s".formatted(thumbnailDir(storagePath), width, thumbnailFilename(storagePath)))
        .normalize();
  }

  /** batch/{batchId}/thumb */
  private String thumbnailDir(String storagePath) {
    int slash = storagePath.lastIndexOf('/');
    return (slash < 0 ? "" : storagePath.substring(0, slash + 1)) + THUMBNAIL_DIR;
  }

  /** 원본 파일명의 확장자를 jpg 로 바꾼 이름 */
  private String thumbnailFilename(String storagePath) {
    String filename = storagePath.substring(storagePath.lastIndexOf('/') + 1);
    int dot = filename.lastIndexOf('.');
    return (dot < 0 ? filename : filename.substring(0, dot)) + ".jpg";
  }

  /** 이미지 헤더만 읽어 포맷과 크기를 판별 */
  private ImageMetadata readMetadata(byte[] bytes) {

    try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {

      ErrorGuard.throwIf(iis == null, ImageErrorCode.INVALID_IMAGE);

      Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
      ErrorGuard.throwIf(!readers.hasNext(), ImageErrorCode.INVALID_IMAGE);

      ImageReader reader = readers.next();
      try {
        reader.setInput(iis);

        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        String contentType = ALLOWED_FORMATS.get(format);

        if (contentType == null) {
          log.info("허용되지 않는 이미지 포맷 - format: {}", format);
          ErrorGuard.fail(ImageErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

        return new ImageMetadata(contentType, reader.getWidth(0), reader.getHeight(0));
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      log.info("이미지 디코딩 실패", e);
      return ErrorGuard.fail(ImageErrorCode.INVALID_IMAGE, e);
    }
  }

  /** batch/{batchId}/{UUID}.{ext} 형태의 저장 경로를 생성 */
  private String generateRelativePath(Long batchId, String contentType) {
    return "%s/%d/%s.%s"
        .formatted(BATCH_DIR_PREFIX, batchId, UUID.randomUUID(), EXTENSIONS.get(contentType));
  }

  private record ImageMetadata(String contentType, int width, int height) {}
}
