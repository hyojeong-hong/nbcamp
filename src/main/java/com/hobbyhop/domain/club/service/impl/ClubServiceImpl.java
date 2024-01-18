package com.hobbyhop.domain.club.service.impl;

import com.hobbyhop.domain.category.entity.Category;
import com.hobbyhop.domain.category.service.CategoryService;
import com.hobbyhop.domain.club.dto.ClubRequestDTO;
import com.hobbyhop.domain.club.dto.ClubResponseDTO;
import com.hobbyhop.domain.club.entity.Club;
import com.hobbyhop.domain.club.repository.ClubRepository;
import com.hobbyhop.domain.club.service.ClubService;
import com.hobbyhop.domain.clubmember.entity.ClubMember;
import com.hobbyhop.domain.clubmember.enums.MemberRole;
import com.hobbyhop.domain.clubmember.service.ClubMemberService;
import com.hobbyhop.domain.user.entity.User;
import com.hobbyhop.global.exception.club.ClubNotFoundException;
import com.hobbyhop.global.exception.clubmember.ClubMemberRoleException;
import com.hobbyhop.global.request.PageRequestDTO;
import com.hobbyhop.global.response.PageResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberService clubMemberService;
    private final CategoryService categoryService;

    @Override
    public PageResponseDTO<ClubResponseDTO> getAllClubs(PageRequestDTO pageRequestDTO) {
        Page<ClubResponseDTO> result = clubRepository.findAll(pageRequestDTO.getPageable("id"),
                pageRequestDTO.getKeyword());

        return PageResponseDTO.<ClubResponseDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(result.toList())
                .total(Long.valueOf(result.getTotalElements()).intValue())
                .build();
    }

    @Override
    public ClubResponseDTO getClub(Long clubId) {
        Club club = findClub(clubId);

        return ClubResponseDTO.fromEntity(club);
    }

    @Override
    @Transactional
    public ClubResponseDTO makeClub(ClubRequestDTO clubRequestDTO, User user) {
        Category category = categoryService.findCategory(clubRequestDTO.getCategoryId());
        Club club = Club.builder().title(clubRequestDTO.getTitle())
                .content(clubRequestDTO.getContent()).category(category).build();
        Club savedClub = clubRepository.save(club);
        clubMemberService.joinClub(club, user);
        return ClubResponseDTO.fromEntity(savedClub);
    }

    @Override
    @Transactional
    public void removeClubById(Long clubId, User user) {
        // 예외 케이스
        // 1. 클럽이 존재하지 않음.
        // 2. 유저가 가입되어있지 않은 클럽임
        // 3. ADMIN 권한이 없음.

        // 예외 케이스 1번
        Club club = findClub(clubId);

        // 예외 케이스 2번
        ClubMember clubMember = clubMemberService.findByClubAndUser(club, user);

        // 예외 케이스 3번
        if (!clubMember.getMemberRole().equals(MemberRole.ADMIN)) {
            throw new ClubMemberRoleException();
        }

        // 멤버 목록 전부 지우기
        clubMemberService.removeMember(club, user);

        //클럽 지우기
        clubRepository.delete(club);
    }

    @Override
    @Transactional
    public ClubResponseDTO modifyClub(Long clubId, ClubRequestDTO clubRequestDTO, User user) {
        // 클럽이 존재하는지
        Club club = findClub(clubId);

        // 클럽에 가입되어있는지
        ClubMember clubMember = clubMemberService.findByClubAndUser(club, user);

        // 예외 케이스 3번
        if (clubMember.getMemberRole() != MemberRole.ADMIN) {
            throw new ClubMemberRoleException();
        }

        if (clubRequestDTO.getTitle() != null) {
            club.changeTitle(clubRequestDTO.getTitle());
        }

        if (clubRequestDTO.getContent() != null) {
            club.changeContent(clubRequestDTO.getContent());
        }

        if (clubRequestDTO.getCategoryId() != null) {
            Category category = categoryService.findCategory(clubRequestDTO.getCategoryId());
            club.changeCategory(category);
        }

        Club modifiedClub = clubRepository.save(club);

        return ClubResponseDTO.fromEntity(modifiedClub);
    }

    @Override
    public List<ClubResponseDTO> getMyClubs(User user) {
        List<ClubMember> list = clubMemberService.findByUserId(user);
        return list.stream().map(clubMember -> ClubResponseDTO.fromEntity(
                clubMember.getClubMemberPK().getClub())).collect(Collectors.toList());
    }

    @Override
    public void removeMember(Long clubId, User user){
        Club club = findClub(clubId);
        clubMemberService.removeMember(club, user);
    }

    @Override
    public Club findClub(Long clubId) {
        return clubRepository.findById(clubId).orElseThrow(ClubNotFoundException::new);
    }
}