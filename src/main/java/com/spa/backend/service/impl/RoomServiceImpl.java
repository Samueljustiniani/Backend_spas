package com.spa.backend.service.impl;

import com.spa.backend.model.Room;
import com.spa.backend.repository.RoomRepository;
import com.spa.backend.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository repo;

    public RoomServiceImpl(RoomRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Room> findAll() { return repo.findAll(); }

    @Override
    public Optional<Room> findById(Long id) { return repo.findById(id); }

    @Override
    public Room save(Room room) { return repo.save(room); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
