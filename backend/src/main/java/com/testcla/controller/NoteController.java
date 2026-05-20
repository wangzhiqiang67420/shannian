package com.testcla.controller;

import com.testcla.entity.Note;
import com.testcla.entity.NoteHistory;
import com.testcla.service.GeocodeService;
import com.testcla.service.NoteService;
import com.testcla.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private GeocodeService geocodeService;

    @Autowired
    private JwtUtil jwtUtil;

    private String getPhone(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getPhoneFromToken(token);
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("message", "操作成功");
        return result;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", msg);
        return result;
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "5") int size) {
        String phone = getPhone(request);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Note> result = noteService.list(phone, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("hasMore", result.getCurrent() < result.getPages());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "操作成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> detail(@RequestParam Long id) {
        Note note = noteService.detail(id);
        if (note == null) {
            return ResponseEntity.ok(error("笔记不存在"));
        }
        return ResponseEntity.ok(success(note));
    }

    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocode(@RequestParam double lat, @RequestParam double lng) {
        String address = geocodeService.reverseGeocode(lat, lng);
        Map<String, Object> data = new HashMap<>();
        data.put("address", address);
        return ResponseEntity.ok(success(data));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(HttpServletRequest request,
                                                    @RequestBody Map<String, String> body) {
        String phone = getPhone(request);
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.ok(error("内容不能为空"));
        }
        Note note = noteService.add(phone, content.trim());

        // Optional: set location if provided
        if (body.containsKey("latitude") && body.containsKey("longitude")) {
            note.setLatitude(new java.math.BigDecimal(body.get("latitude")));
            note.setLongitude(new java.math.BigDecimal(body.get("longitude")));
        }
        if (body.containsKey("address") && body.get("address") != null) {
            note.setAddress(body.get("address"));
        }

        // Update note with location info if provided
        if (note.getLatitude() != null) {
            noteService.updateLocation(note.getId(), note.getLatitude(), note.getLongitude(), note.getAddress());
        }

        return ResponseEntity.ok(success(note));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.ok(error("内容不能为空"));
        }
        Note note = noteService.update(id, content.trim());
        if (note == null) {
            return ResponseEntity.ok(error("笔记不存在"));
        }
        return ResponseEntity.ok(success(note));
    }

    @GetMapping("/histories")
    public ResponseEntity<Map<String, Object>> histories(@RequestParam Long id) {
        List<NoteHistory> histories = noteService.histories(id);
        return ResponseEntity.ok(success(histories));
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        boolean deleted = noteService.delete(id);
        if (!deleted) {
            return ResponseEntity.ok(error("笔记不存在"));
        }
        return ResponseEntity.ok(success(null));
    }
}
