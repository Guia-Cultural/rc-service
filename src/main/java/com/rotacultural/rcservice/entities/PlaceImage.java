package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "place_images")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idLoc", nullable = false)
    private Places place;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String url;

    @Size(max = 255)
    @Column(name = "original_name")
    private String originalName;

    @Size(max = 100)
    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "is_cover")
    private Boolean cover = false;

    @CreationTimestamp
    @Column(name = "date_create", nullable = false, updatable = false)
    private LocalDateTime dateCreate;
}
