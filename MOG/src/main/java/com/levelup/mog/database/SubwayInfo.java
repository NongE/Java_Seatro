package com.levelup.mog.database;

import com.levelup.mog.database.emb.SubwayIdFkEmb;
import com.levelup.mog.model.dto.SubwayIdDto;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "subway_info")
public class SubwayInfo {

    @EmbeddedId
    private SubwayIdFkEmb subwayIdFkEmb;

    @Column(name = "tel_number")
    private String telNumber;

    @Column(name = "address")
    private String address;

    public SubwayIdDto SubwayIdToDto(){
        return new SubwayIdDto(subwayIdFkEmb.getLineNumber(), subwayIdFkEmb.getStationName());
    }

    public String getTelNumber() {
        return telNumber;
    }

    public String getAddress() {
        return address;
    }
}
