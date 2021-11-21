package com.pmeller.altairusefulscraper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Estate {
    @CreatedDate
    LocalDateTime createdAt;
    @Column(length = 20, nullable = false)
    String source;
    @Column(nullable = false)
    String name;
    @Column(precision = 10, scale = 2, nullable = false)
    Double price;
    @Column(precision = 10, scale = 2)
    Double pricePerMeter;
    @Column(precision = 10, scale = 2)
    Double area;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
