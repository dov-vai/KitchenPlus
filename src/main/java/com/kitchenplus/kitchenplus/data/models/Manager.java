package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends User {
}
