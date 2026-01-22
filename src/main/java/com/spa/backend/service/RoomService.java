package com.spa.backend.service;

import com.spa.backend.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    List<Room> findAll();
    Optional<Room> findById(Long id);
    Room save(Room room);
    void deleteById(Long id);
}
