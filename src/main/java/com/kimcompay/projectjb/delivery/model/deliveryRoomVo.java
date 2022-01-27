package com.kimcompay.projectjb.delivery.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "delivery_rooms")
@Entity
public class deliveryRoomVo {
    @Id
    @Column(name = "room_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roomId;


    @Column(name = "company_id")
    private int companyId;

    @Column(name = "start_flag",columnDefinition = "TINYINT")
    private int startFlag;

    @Column(name = "deliver_room_flag",columnDefinition = "TINYINT")
    private int deliverRoomDoneFlag;




}
