package com.hcjc666.tcpserver.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.hcjc666.tcpserver.entity.DtuInfo;

@Repository
public interface DtuInfoMapper {
    public List<DtuInfo> getList();

    public void update(DtuInfo dtuInfo);
}
