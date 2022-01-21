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
    @Column(name = "dr_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int drId;

    @Column(name = "deliver_room_master")
    private int deliverRoomMaster;

    
    @Column(name = "deliver_room_customerids")//주문내역에서 주문요청한 유저들 아이디 넣기
    private String deliverRoomCustomerIds;



}
