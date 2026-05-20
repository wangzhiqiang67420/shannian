package com.testcla.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testcla.entity.Note;
import com.testcla.entity.NoteHistory;
import com.testcla.mapper.NoteHistoryMapper;
import com.testcla.mapper.NoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NoteHistoryMapper noteHistoryMapper;

    public Page<Note> list(String phone, int page, int size) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .eq(Note::getPhone, phone)
                .orderByDesc(Note::getUpdatedAt);
        Page<Note> p = new Page<>(page, size);
        return noteMapper.selectPage(p, wrapper);
    }

    public Note detail(Long id) {
        return noteMapper.selectById(id);
    }

    @Transactional
    public Note add(String phone, String content) {
        Note note = new Note();
        note.setPhone(phone);
        note.setContent(content);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return note;
    }

    @Transactional
    public void updateLocation(Long id, java.math.BigDecimal latitude, java.math.BigDecimal longitude, String address) {
        Note note = noteMapper.selectById(id);
        if (note == null) return;
        note.setLatitude(latitude);
        note.setLongitude(longitude);
        note.setAddress(address);
        noteMapper.updateById(note);
    }

    @Transactional
    public Note update(Long id, String content) {
        Note note = noteMapper.selectById(id);
        if (note == null) return null;

        NoteHistory history = new NoteHistory();
        history.setNoteId(id);
        history.setContent(note.getContent());
        history.setCreatedAt(LocalDateTime.now());
        noteHistoryMapper.insert(history);

        note.setContent(content);
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        return note;
    }

    public List<NoteHistory> histories(Long noteId) {
        LambdaQueryWrapper<NoteHistory> wrapper = new LambdaQueryWrapper<NoteHistory>()
                .eq(NoteHistory::getNoteId, noteId)
                .orderByDesc(NoteHistory::getCreatedAt);
        return noteHistoryMapper.selectList(wrapper);
    }

    @Transactional
    public boolean delete(Long id) {
        if (noteMapper.selectById(id) == null) return false;
        LambdaQueryWrapper<NoteHistory> wrapper = new LambdaQueryWrapper<NoteHistory>()
                .eq(NoteHistory::getNoteId, id);
        noteHistoryMapper.delete(wrapper);
        noteMapper.deleteById(id);
        return true;
    }
}
