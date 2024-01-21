package com.hobbyhop.domain.post.service.impl;

import com.hobbyhop.domain.club.entity.Club;
import com.hobbyhop.domain.club.service.ClubService;
import com.hobbyhop.domain.clubmember.entity.ClubMember;
import com.hobbyhop.domain.clubmember.enums.MemberRole;
import com.hobbyhop.domain.clubmember.service.ClubMemberService;
import com.hobbyhop.domain.post.dto.PostPageResponseDTO;
import com.hobbyhop.domain.post.dto.PostRequestDTO;
import com.hobbyhop.domain.post.dto.PostResponseDTO;
import com.hobbyhop.domain.post.entity.Post;
import com.hobbyhop.domain.post.repository.PostRepository;
import com.hobbyhop.domain.post.s3.S3Service;
import com.hobbyhop.domain.post.service.PostService;
import com.hobbyhop.domain.postuser.service.PostUserService;
import com.hobbyhop.domain.user.entity.User;
import com.hobbyhop.global.exception.clubmember.ClubMemberNotFoundException;
import com.hobbyhop.global.exception.common.UnAuthorizedModifyException;
import com.hobbyhop.global.exception.post.PostNotCorrespondUser;
import com.hobbyhop.global.exception.post.PostNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final ClubService clubService;
    private final PostUserService postUserService;
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final ClubMemberService clubMemberService;

    @Override
    public Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
    }

    @Override
    @Transactional
    public PostResponseDTO makePost(User user, Long clubId, PostRequestDTO postRequestDTO) {
        if(clubMemberService.isClubMember(clubId, user.getId()))
            throw new ClubMemberNotFoundException();

        Club club = clubService.findClub(clubId);

        clubMemberService.findByClubAndUser(clubId, user.getId());

        Post post = Post.builder()
                .postTitle(postRequestDTO.getPostTitle())
                .postContent(postRequestDTO.getPostContent())
                .club(club)
                .user(user)
                .likeCnt(0L)
                .build();

        postRepository.save(post);

        return PostResponseDTO.fromEntity(post);
    }

    @Override
    @Transactional
    public void imageUploadPost(User user, Long clubId, Long postId, MultipartFile file) {
        ClubMember clubMember = clubMemberService.findByClubAndUser(clubId, user.getId());

        if(!clubMember.getMemberRole().equals(MemberRole.ADMIN))
            throw new UnAuthorizedModifyException();

        Post post = findAndCheckPostAndClub(clubId, postId);

        if(!post.getUser().getId().equals(user.getId()))
            throw new PostNotCorrespondUser();

        String originFilename = s3Service.saveFile(file);
        String savedFilename = UUID.randomUUID() + "_" + originFilename;

        post.changeImageUrl(originFilename, savedFilename);
    }


    @Override
    public PostResponseDTO getPostById(Long clubId, Long postId) {
        Post post = findAndCheckPostAndClub(clubId, postId);

        return PostResponseDTO.fromEntity(post);
    }

    public Post findAndCheckPostAndClub(Long clubId, Long postId){
        Club club = clubService.findClub(clubId);

        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if(!club.getId().equals(post.getClub().getId())){
            throw new PostNotCorrespondUser();
        }

        return post;
    }

    @Override
    public PostPageResponseDTO getAllPost(Pageable pageable, Long clubId) {
        return postRepository.findAllByClubId(pageable, clubId);
    }

    @Override
    @Transactional
    public PostResponseDTO modifyPost(User user, Long clubId, Long postId, MultipartFile file, PostRequestDTO postRequestDTO) {
        ClubMember clubMember = clubMemberService.findByClubAndUser(clubId, user.getId());

        if(!clubMember.getMemberRole().equals(MemberRole.ADMIN))
            throw new UnAuthorizedModifyException();

        Post post = findAndCheckPostAndClub(clubId, postId);

        if(!post.getUser().getId().equals(user.getId()))
            throw new PostNotCorrespondUser();

        String originFilename = s3Service.saveFile(file);
        String savedFilename = UUID.randomUUID() + "_" + originFilename;

        if(postRequestDTO.getPostTitle() != null) {
            post.changeTitle(postRequestDTO.getPostTitle());
        }

        if(postRequestDTO.getPostContent() != null) {
            post.changeContent(postRequestDTO.getPostContent());
        }

        if(originFilename != null) {
            post.changeImageUrl(originFilename, savedFilename);
        }

        return PostResponseDTO.fromEntity(post);
    }

    @Override
    @Transactional
    public void deletePost(User user, Long clubId, Long postId){
        ClubMember clubMember = clubMemberService.findByClubAndUser(clubId, user.getId());

        if(!clubMember.getMemberRole().equals(MemberRole.ADMIN))
            throw new UnAuthorizedModifyException();

        Post post = findAndCheckPostAndClub(clubId, postId);

        if(!post.getUser().getId().equals(user.getId()))
            throw new PostNotCorrespondUser();

        postRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public void makePostUser(User user, Long clubId, Long postId){
        Post post = findAndCheckPostAndClub(clubId, postId);

        postUserService.postUser(user, post);
    }
}
