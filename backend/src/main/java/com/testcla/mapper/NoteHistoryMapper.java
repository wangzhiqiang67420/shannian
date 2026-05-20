package com.testcla.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testcla.entity.NoteHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteHistoryMapper extends BaseMapper<NoteHistory> {
}
