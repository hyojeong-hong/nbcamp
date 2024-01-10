package com.hobbyhop.domain.club.service.impl;

import com.hobbyhop.domain.category.entity.Category;
import com.hobbyhop.domain.category.repository.CategoryRepository;
import com.hobbyhop.domain.club.dto.ClubRequestDTO;
import com.hobbyhop.domain.club.dto.ClubResponseDTO;
import com.hobbyhop.domain.club.entity.Club;
import com.hobbyhop.domain.club.repository.ClubRepository;
import com.hobbyhop.domain.club.service.ClubService;
import com.hobbyhop.global.exception.category.CategoryNotFoundException;
import com.hobbyhop.global.exception.club.ClubNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ClubServiceImpl implements ClubService {
    private final ClubRepository clubRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ClubResponseDTO> getAllClubs() {
        List<ClubResponseDTO> list = clubRepository.findAll().stream().map(club ->
                ClubResponseDTO.builder()
                        .id(club.getId())
                        .title(club.getTitle())
                        .content(club.getContent())
                        .createdAt(club.getCreatedAt())
                        .modifiedAt(club.getModifiedAt())
                        .build()
        ).collect(Collectors.toList());

        return list;
    }

    @Override
    public ClubResponseDTO getClub(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(CategoryNotFoundException::new);

        return ClubResponseDTO.fromEntity(club);
    }

    @Override
    @Transactional
    public ClubResponseDTO makeClub(ClubRequestDTO clubRequestDTO) {
        Category category = categoryRepository.findById(clubRequestDTO.getCategoryId()).orElseThrow(CategoryNotFoundException::new);

        Club club = Club.builder()
                .title(clubRequestDTO.getTitle())
                .content(clubRequestDTO.getContent())
                .category(category)
                .build();

        Club savedClub = clubRepository.save(club);

        return ClubResponseDTO.fromEntity(savedClub);
    }

    @Override
    @Transactional
    public void removeClubById(Long clubId) {
        clubRepository.deleteById(clubId);
    }

    @Override
    @Transactional
    public ClubResponseDTO modifyClub(Long clubId, ClubRequestDTO clubRequestDTO) {
        Club club = clubRepository.findById(clubId).orElseThrow(ClubNotFoundException::new);

        if(clubRequestDTO.getTitle() != null) {
            club.changeTitle(clubRequestDTO.getTitle());
        }

        if(clubRequestDTO.getContent() != null) {
            club.changeContent(clubRequestDTO.getContent());
        }
        if(clubRequestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(clubRequestDTO.getCategoryId()).orElseThrow(
                    CategoryNotFoundException::new);
            club.changeCategory(category);
        }

        Club modifiedClub = clubRepository.save(club);

        return ClubResponseDTO.fromEntity(modifiedClub);
    }
}
