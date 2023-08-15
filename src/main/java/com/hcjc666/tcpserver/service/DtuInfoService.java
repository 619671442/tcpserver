package com.hcjc666.tcpserver.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcjc666.tcpserver.entity.DtuInfo;
import com.hcjc666.tcpserver.mapper.DtuInfoMapper;

@Service
public class DtuInfoService {
    @Autowired
    private DtuInfoMapper dtuInfoMapper;

    public List<DtuInfo> getList() {
        return dtuInfoMapper.getList();
    }
    public void update(DtuInfo dtuInfo) {
        dtuInfoMapper.update(dtuInfo);
    }
}