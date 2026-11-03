/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.domain.assignment.mapper;

import org.springframework.stereotype.Component;

import com.signtelecom.datalabelbe.domain.assignment.dto.response.AssignmentResponse;
import com.signtelecom.datalabelbe.domain.assignment.entity.Assignment;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentAction;
import com.signtelecom.datalabelbe.domain.assignment.entity.AssignmentHistory;
import com.signtelecom.datalabelbe.domain.image.entity.Image;
import com.signtelecom.datalabelbe.domain.image.entity.ImageBatch;
import com.signtelecom.datalabelbe.domain.image.mapper.ImageMapper;
import com.signtelecom.datalabelbe.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssignmentMapper {

  private final ImageMapper imageMapper;

  /**
   * 이미지와 작업자를 배정한 Assignment 엔티티를 생성
   *
   * <p>상태는 엔티티 기본값인 IN_PROGRESS(진행중)로 시작. 할당이 생긴 순간부터 작업이 시작된 것으로 보며, PENDING(대기)은 미할당을 뜻하므로 할당
   * 레코드에는 쓰지 않음
   *
   * @param image 할당할 이미지
   * @param worker 배정할 작업자
   * @param assignedBy 배정한 관리자
   * @return 신규 생성된 Assignment 엔티티
   */
  public Assignment toAssignment(Image image, User worker, User assignedBy) {
    return Assignment.builder().image(image).worker(worker).assignedBy(assignedBy).build();
  }

  /**
   * 할당 변경 이력 엔티티를 생성
   *
   * @param image 대상 이미지
   * @param action 변경 종류
   * @param worker 배정·재배정은 새 담당자, 해제는 해제된 담당자
   * @param previousWorker 재배정 전 담당자. 재배정이 아니면 null
   * @param actedBy 변경을 수행한 관리자
   * @return 신규 생성된 AssignmentHistory 엔티티
   */
  public AssignmentHistory toHistory(
      Image image, AssignmentAction action, User worker, User previousWorker, User actedBy) {
    return AssignmentHistory.builder()
        .image(image)
        .action(action)
        .worker(worker)
        .previousWorker(previousWorker)
        .actedBy(actedBy)
        .build();
  }

  /**
   * Assignment 엔티티를 할당 응답 DTO로 변환
   *
   * <p>이미지 파일 조회 경로와 해상도를 함께 담음
   *
   * @param assignment 할당 엔티티
   * @param objectCount 라벨링된 인물 객체 수
   * @param labelSaved 라벨 저장 여부
   * @return 할당 응답 DTO
   */
  public AssignmentResponse toAssignmentResponse(
      Assignment assignment, long objectCount, boolean labelSaved) {
    Image image = assignment.getImage();
    ImageBatch batch = image.getBatch();
    Long batchId = batch.getId();

    return AssignmentResponse.builder()
        .assignmentId(assignment.getId())
        .imageId(image.getId())
        .batchId(batchId)
        .batchName(batch.getName())
        .objectCount(objectCount)
        .labelSaved(labelSaved)
        .filename(image.getOriginalFilename())
        .url(imageMapper.toImageFileUrl(batchId, image.getId()))
        .thumbnailUrl(imageMapper.toThumbnailUrl(batchId, image.getId()))
        .width(image.getWidth())
        .height(image.getHeight())
        .workerId(assignment.getWorker().getId())
        .workerName(assignment.getWorker().getName())
        .assignedById(assignment.getAssignedBy().getId())
        .assignedByName(assignment.getAssignedBy().getName())
        .status(assignment.getStatus())
        .createdAt(assignment.getCreatedAt())
        .updatedAt(assignment.getUpdatedAt())
        .build();
  }
}
