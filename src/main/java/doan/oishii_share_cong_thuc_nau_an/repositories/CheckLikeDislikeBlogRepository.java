package doan.oishii_share_cong_thuc_nau_an.repositories;

import doan.oishii_share_cong_thuc_nau_an.common.vo.CheckLikeDislikeReportVo;
import doan.oishii_share_cong_thuc_nau_an.web.entities.CheckLikeDislikeBlog;
import doan.oishii_share_cong_thuc_nau_an.web.entities.CheckLikeDislikeReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckLikeDislikeBlogRepository extends JpaRepository<CheckLikeDislikeBlog, Integer> {

    @Query("select new doan.oishii_share_cong_thuc_nau_an.common.vo.CheckLikeDislikeReportVo(" +
            " c.checkLike, c.checkDislike)" +
            " from CheckLikeDislikeBlog c where c.account.accountId =:accountId and c.blog.blogID =:blogId")
    public CheckLikeDislikeReportVo getCheckLikeDislikeBlog(Integer blogId, Integer accountId);
}
