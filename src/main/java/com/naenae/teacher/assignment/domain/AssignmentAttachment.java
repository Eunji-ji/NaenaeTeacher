package com.naenae.teacher.assignment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Entity @Table(name = "assignment_attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assignment_id", nullable = false) private Assignment assignment;
    @Column(name="original_name", nullable=false, length=255) private String originalName;
    @Column(name="stored_name", nullable=false, unique=true, length=255) private String storedName;
    @Column(name="content_type", length=150) private String contentType;
    @Column(name="file_size", nullable=false) private long fileSize;
    static AssignmentAttachment create(Assignment a,String o,String s,String c,long z){AssignmentAttachment v=new AssignmentAttachment();v.assignment=a;v.originalName=o;v.storedName=s;v.contentType=c;v.fileSize=z;return v;}
}
