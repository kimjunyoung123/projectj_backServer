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
@Table(name = "deliver_room_details")
@Entity
public class deliverRoomDetailVo {

    @Id
    @Column(name = "dd_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ddId;
    @Column(name = "room_id")
    private int roomId;
    @Column(name = "user_id")
    private int userId;
    @Column(name = "done_flag",columnDefinition = "TINYINT")
    private int  doneFlag;
    @Column(name = "deliver_room_details_address",nullable = false,length = 50)
    private String  address;

}
