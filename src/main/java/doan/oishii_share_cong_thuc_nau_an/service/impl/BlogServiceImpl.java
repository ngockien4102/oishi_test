package doan.oishii_share_cong_thuc_nau_an.service.impl;

import doan.oishii_share_cong_thuc_nau_an.Exception.ErrorCode;
import doan.oishii_share_cong_thuc_nau_an.Exception.NotFoundException;
import doan.oishii_share_cong_thuc_nau_an.common.vo.BlogVo;
import doan.oishii_share_cong_thuc_nau_an.common.vo.CheckLikeDislikeReportVo;
import doan.oishii_share_cong_thuc_nau_an.common.vo.MessageVo;
import doan.oishii_share_cong_thuc_nau_an.repositories.BlogRepository;
import doan.oishii_share_cong_thuc_nau_an.repositories.CheckLikeDislikeBlogRepository;
import doan.oishii_share_cong_thuc_nau_an.service.BlogService;
import doan.oishii_share_cong_thuc_nau_an.web.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private CheckLikeDislikeBlogRepository checkLikeDislikeBlogRepository;

    @Override
    public Page<BlogVo> getListBlogActive(String searchData, Integer pageIndex, Integer pageSize) {
        if (searchData == null) {
            searchData = "";
        }
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return blogRepository.getListBlogActive("%" + searchData.trim() + "%", pageable);
    }

    @Override
    public Page<BlogVo> getListBlogPending(String searchData,Integer pageIndex, Integer pageSize) {
        if (searchData == null) {
            searchData = "";
        }
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return blogRepository.getListBlogPending("%" + searchData.trim() + "%", pageable);
    }

    @Override
    public BlogVo getBlogDetail(Integer blogId) {
        return blogRepository.getBlogDetail(blogId);
    }

    @Override
    public ResponseEntity<?> createBlog(String title, String content, Account account) {
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setAccount(account);
        if (account.getRole().equals("ROLE_ADMIN") || account.getRole().equals("ROLE_MOD")) {
            blog.setStatus(1);
        } else {
            blog.setStatus(0);
        }
        blog.setTotalLike(0);
        blog.setTotalDisLike(0);
        blog.setNumberComment(0);
        blogRepository.save(blog);
        return ResponseEntity.ok(new MessageVo("l??u b??i vi???t th??nh c??ng", "success"));
    }

    @Override
    public ResponseEntity<?> updateBlog(Integer blogId, String title, String content, Account account) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() ->
                new NotFoundException(ErrorCode.Not_Found, "blog" + blogId + " Not exist or blog was blocked "));
        if (blog.getAccount().getAccountId() == account.getAccountId()) {
            blog.setTitle(title);
            blog.setAccount(account);
            blog.setContent(content);
            blogRepository.save(blog);
            return ResponseEntity.ok(new MessageVo("c???p nh???t b??i vi???t th??nh c??ng", "success"));
        } else {
            return ResponseEntity.ok(new MessageVo("B???n kh??ng c?? quy???n s???a b??i vi???t n??y", "error"));
        }
    }

    @Override
    public ResponseEntity<?> deleteBlog(Integer blogId, Account account) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() ->
                new NotFoundException(ErrorCode.Not_Found, "blog" + blogId + " Not exist or blog was blocked "));
        if (blog.getAccount().getAccountId() == account.getAccountId()) {
            blog.setStatus(3);
            blogRepository.save(blog);
            return ResponseEntity.ok(new MessageVo("X??a b??i vi???t th??nh c??ng", "success"));
        } else if (account.getRole().equals("ROLE_ADMIN")) {
            blog.setStatus(3);
            blogRepository.save(blog);
            return ResponseEntity.ok(new MessageVo("X??a b??i vi???t th??nh c??ng", "success"));
        } else {
            return ResponseEntity.ok(new MessageVo("B???n kh??ng c?? quy???n x??a b??i vi???t n??y", "error"));
        }
    }

    @Override
    public ResponseEntity<?> approveBlog(Integer blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() ->
                new NotFoundException(ErrorCode.Not_Found, "blog" + blogId + " Not exist or blog was blocked "));
        blog.setStatus(1);
        blogRepository.save(blog);
        return ResponseEntity.ok(new MessageVo("???? ph?? duy???t b??i vi???t", "success"));
    }

    @Override
    public ResponseEntity<?> likeBlog(Integer blogId, Account account) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() ->
                new NotFoundException(ErrorCode.Not_Found, "blog" + blogId + " Not exist or blog was blocked "));
        CheckLikeDislikeReportVo checkLikeOrDislikes = checkLikeDislikeBlogRepository.getCheckLikeDislikeBlog(blogId, account.getAccountId());
        CheckLikeDislikeBlog checkLikeDislikeBlog = new CheckLikeDislikeBlog();
        checkLikeDislikeBlog.setBlog(blog);
        checkLikeDislikeBlog.setAccount(account);
        checkLikeDislikeBlog.setCheckId(new CheckLikeDislikeReportId(account.getAccountId(), blogId));
        if (checkLikeOrDislikes == null) { //ch??a like , dislike  bao gi???
            blog.setTotalLike(blog.getTotalLike() + 1); //t??ng total like l??n 1
            blog.setTotalDisLike(blog.getTotalDisLike());// gi??? nguy??n total dislike
            checkLikeDislikeBlog.setCheckLike(1);//like
            checkLikeDislikeBlog.setCheckDislike(0);//ko dislike
        } else if (checkLikeOrDislikes.getCheckLike() == null || checkLikeOrDislikes.getCheckLike() != 1) { //ng?????i ????ng nh???p ch??a like, ???? dislike
            blog.setTotalLike(blog.getTotalLike() + 1); //t??ng total like l??n 1
            if (checkLikeOrDislikes.getCheckDislike() == 1 && blog.getTotalDisLike() > 0) { //n???u ??ang dislike
                blog.setTotalDisLike(blog.getTotalDisLike() - 1); //gi???m total dislike ??i 1
            }
            checkLikeDislikeBlog.setCheckLike(1); //like
            checkLikeDislikeBlog.setCheckDislike(0); //b??? dislike
        } else { // ng?????i ????ng nh???p ???? like => b???m n??t ????? b??? like
            if (blog.getTotalLike() > 0) {
                blog.setTotalLike(blog.getTotalLike() - 1); // gi???m total like xu???ng 1
            }
            blog.setTotalDisLike(blog.getTotalDisLike());// gi??? nguy??n total dislike
            checkLikeDislikeBlog.setCheckLike(0); //b??? like
            checkLikeDislikeBlog.setCheckDislike(checkLikeOrDislikes.getCheckDislike()); // gi??? nguy??n dislike
        }
        checkLikeDislikeBlogRepository.save(checkLikeDislikeBlog);
        blogRepository.save(blog);
        return ResponseEntity.ok(new MessageVo("???? b???m n??t like b??i vi???t", "success"));
    }

    @Override
    public ResponseEntity<?> dislikeBlog(Integer blogId, Account account) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() ->
                new NotFoundException(ErrorCode.Not_Found, "blog" + blogId + " Not exist or blog was blocked "));
        CheckLikeDislikeReportVo checkLikeOrDislikes = checkLikeDislikeBlogRepository.getCheckLikeDislikeBlog(blogId, account.getAccountId());
        CheckLikeDislikeBlog checkLikeDislikeBlog = new CheckLikeDislikeBlog();
        checkLikeDislikeBlog.setBlog(blog);
        checkLikeDislikeBlog.setAccount(account);
        checkLikeDislikeBlog.setCheckId(new CheckLikeDislikeReportId(account.getAccountId(), blogId));
        if (checkLikeOrDislikes == null) { //ch??a like , dislike hay report bao gi???
            blog.setTotalLike(blog.getTotalLike()); //gi??? nguy??n total like
            blog.setTotalDisLike(blog.getTotalDisLike() + 1);// t??ng total dislike l??n 1
            checkLikeDislikeBlog.setCheckLike(0);//ko like
            checkLikeDislikeBlog.setCheckDislike(1);//dislike

        } else if (checkLikeOrDislikes.getCheckDislike() == null || checkLikeOrDislikes.getCheckDislike() != 1) { //ng?????i ????ng nh???p ch??a dislike, ???? like ho???c report
            blog.setTotalDisLike(blog.getTotalDisLike() + 1); //t??ng total dislike l??n 1
            if (checkLikeOrDislikes.getCheckLike() == 1 && blog.getTotalLike() > 0) { //n???u ???? t???ng like
                blog.setTotalLike(blog.getTotalLike() - 1); //gi???m total like ??i 1
            }
            checkLikeDislikeBlog.setCheckLike(0); //b??? like
            checkLikeDislikeBlog.setCheckDislike(1); // dislike

        } else { // ng?????i ????ng nh???p ???? dislike => b???m n??t ????? b??? dislike
            if (blog.getTotalDisLike() > 0) {
                blog.setTotalDisLike(blog.getTotalDisLike() - 1); // gi???m total dislike xu???ng 1
            }
            blog.setTotalLike(blog.getTotalLike());// gi??? nguy??n total like
            checkLikeDislikeBlog.setCheckDislike(0); //b??? dislike
            checkLikeDislikeBlog.setCheckLike(checkLikeOrDislikes.getCheckLike()); // gi??? nguy??n like
        }
        checkLikeDislikeBlogRepository.save(checkLikeDislikeBlog);
        blogRepository.save(blog);
        return ResponseEntity.ok(new MessageVo("???? b???m n??t dislike b??i vi???t", "success"));
    }


}



