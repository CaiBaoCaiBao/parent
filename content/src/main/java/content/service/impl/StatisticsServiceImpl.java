package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import common.client.SocialClient;
import common.client.UsersClient;
import common.utils.Result;
import content.pojo.entity.Attraction;
import content.pojo.entity.Destination;
import content.pojo.entity.TravelNote;
import content.pojo.vo.StatisticsOverviewVO;
import content.service.AttractionService;
import content.service.DestinationService;
import content.service.StatisticsService;
import content.service.TravelNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 系统统计服务实现
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UsersClient usersClient;

    @Autowired
    private SocialClient socialClient;

    @Autowired
    private TravelNoteService travelNoteService;

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private AttractionService attractionService;

    /**
     * 获取系统数据概况
     */
    @Override
    public Result<StatisticsOverviewVO> getStatisticsOverview() {
        StatisticsOverviewVO vo = new StatisticsOverviewVO();

        // 获取用户总数（通过UsersClient调用users服务）
        try {
            Result<?> userCountResult = usersClient.getUserCount();
            if (userCountResult != null && userCountResult.getSuccess() && userCountResult.getData() != null) {
                Map<String, Object> userData = (Map<String, Object>) userCountResult.getData();
                vo.setTotalUsers(((Number) userData.get("userCount")).longValue());
            } else {
                log.warn("获取用户总数失败，返回结果: {}", userCountResult);
                vo.setTotalUsers(0L);
            }
        } catch (Exception e) {
            log.error("获取用户总数失败", e);
            vo.setTotalUsers(0L);
        }

        // 获取游记总数
        QueryWrapper<TravelNote> travelNoteWrapper = new QueryWrapper<>();
        travelNoteWrapper.eq("deleted", 0);
        vo.setTotalTravelNotes(travelNoteService.count(travelNoteWrapper));

        // 获取评论总数（通过SocialClient调用social服务）
        try {
            Result<?> commentCountResult = socialClient.getCommentCount();
            if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                Map<String, Object> commentData = (Map<String, Object>) commentCountResult.getData();
                vo.setTotalComments(((Number) commentData.get("commentCount")).longValue());
            } else {
                log.warn("获取评论总数失败，返回结果: {}", commentCountResult);
                vo.setTotalComments(0L);
            }
        } catch (Exception e) {
            log.error("获取评论总数失败", e);
            vo.setTotalComments(0L);
        }

        // 获取目的地总数
        QueryWrapper<Destination> destinationWrapper = new QueryWrapper<>();
        destinationWrapper.eq("deleted", 0);
        vo.setTotalDestinations(destinationService.count(destinationWrapper));

        // 获取景点总数
        QueryWrapper<Attraction> attractionWrapper = new QueryWrapper<>();
        attractionWrapper.eq("deleted", 0);
        vo.setTotalAttractions(attractionService.count(attractionWrapper));

        return Result.success(vo);
    }
}
