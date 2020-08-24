package com.hirisun.cloud.order.controller.manage;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hirisun.cloud.api.workflow.WorkflowApi;
import com.hirisun.cloud.common.annotation.LoginUser;
import com.hirisun.cloud.common.contains.ApplyInfoStatus;
import com.hirisun.cloud.common.contains.WorkflowNodeAbilityType;
import com.hirisun.cloud.common.exception.CustomException;
import com.hirisun.cloud.common.util.ExceptionPrintUtil;
import com.hirisun.cloud.common.util.UUIDUtil;
import com.hirisun.cloud.common.util.WorkflowUtil;
import com.hirisun.cloud.common.vo.QueryResponseResult;
import com.hirisun.cloud.model.apply.FallBackVO;
import com.hirisun.cloud.model.user.UserVO;
import com.hirisun.cloud.model.workflow.WorkflowActivityVO;
import com.hirisun.cloud.model.workflow.WorkflowInstanceVO;
import com.hirisun.cloud.model.workflow.WorkflowNodeVO;
import com.hirisun.cloud.order.bean.apply.ApplyFeedback;
import com.hirisun.cloud.order.bean.apply.ApplyInfo;
import com.hirisun.cloud.order.bean.apply.ApplyReviewRecord;
import com.hirisun.cloud.order.continer.HandlerWrapper;
import com.hirisun.cloud.order.service.apply.ApplyFeedbackService;
import com.hirisun.cloud.order.service.apply.ApplyInfoService;
import com.hirisun.cloud.order.service.apply.ApplyReviewRecordService;
import com.hirisun.cloud.order.vo.ApproveVO;
import com.hirisun.cloud.order.vo.ImplRequest;
import com.hirisun.cloud.order.vo.OrderCode;
import com.hirisun.cloud.redis.lock.DistributeLock;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 申请信息表 前端控制器
 * </p>
 *
 * @author wuxiaoxing
 * @since 2020-08-08
 */
@Api(tags = "申请审批工单管理")
@RestController
@RequestMapping("/order/applyInfoManage")
public class ApplyInfoManageController {

    private static final Logger logger = LoggerFactory.getLogger(ApplyInfoManageController.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DistributeLock lock;

    @Autowired
    private ApplyInfoService applyInfoService;

    @Autowired
    private ApplyReviewRecordService applyReviewRecordService;

    @Autowired
    private ApplyFeedbackService applyFeedbackService;

    @Autowired
    private WorkflowApi workflowApi;



    @ApiOperation("申请审批记录列表")
    @GetMapping("/page")
    public QueryResponseResult<ApplyInfo> page(
            @LoginUser UserVO user,
            @ApiParam("页码") @RequestParam(required = false,defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(required = false,defaultValue = "20") Integer pageSize,
            @ApiParam(value = "资源类型 1IAAS 2DAAS 3PAAS 5SAAS",required = true) @RequestParam(required = true) Integer resourceType,
            @ApiParam("申请人") @RequestParam(required = false) String creatorName,
            @ApiParam("开始时间") @RequestParam(required = false) String startDate,
            @ApiParam("结束时间") @RequestParam(required = false) String endDate,
            @ApiParam("状态, 1:待审核 2:待实施 3:使用中 4:已删除 5:审核驳回 6:实施驳回") @RequestParam(required = false) String status,
            @ApiParam("处理人,0:全部 1:我 2:其它人") @RequestParam(required = false, defaultValue = "0") String processType,
            @ApiParam("订单号") @RequestParam(required = false) String orderNumber,
            @ApiParam("应用名称") @RequestParam(required = false) String appName,
            @ApiParam("服务名称") @RequestParam(required = false) String serviceName){

        Map<String, Object> param = new HashMap<>();
        param.put("user", user);
        param.put("userName", dealNameforQuery(creatorName));
        param.put("status", status);
        param.put("orderNumber", dealNameforQuery(orderNumber));
        param.put("appName", dealNameforQuery(appName));
        param.put("resourceType", resourceType);
        param.put("serviceName", dealNameforQuery(serviceName));
        param.put("processType", processType);
        param.put("startDate", startDate);
        param.put("endDate", endDate);

        Page<ApplyInfo> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page=applyInfoService.getPage(page,user,param);
        return QueryResponseResult.success(page);
    }

    @ApiOperation("申请审批记录详情")
    @GetMapping("/detail")
    public QueryResponseResult<ApplyInfo> detail(
            @LoginUser UserVO user,
            @ApiParam(value = "详情id",required = true) @RequestParam(required = true,defaultValue = "1") String id){
        return applyInfoService.detail(id);
    }

    public static String dealNameforQuery(String name){
        if(StringUtils.isNotBlank(name)){
            name = name.trim();
            StringBuilder sb = new StringBuilder().append("%").append(name).append("%");
            return sb.toString();
        }
        return null;
    }

    @ApiOperation(value = "审批删除")
    @PostMapping("/delete")
    public QueryResponseResult deleteById(@LoginUser UserVO user,
                                          @ApiParam(value = "审批工单id",required = true)@RequestParam String id) {
        String uuid = UUIDUtil.getUUID();
        String lockKey = id.intern();
        try {
            if (lock.lock(lockKey, uuid)) {
                applyInfoService.deleteById(user, id);
            } else {
                return QueryResponseResult.fail("系统繁忙，请稍后重试！");
            }
        } catch (Exception e) {
            logger.error(ExceptionPrintUtil.getStackTraceInfo(e));
            return QueryResponseResult.fail("删除失败！");
        } finally {
            lock.unlock(lockKey, uuid);
        }
        return QueryResponseResult.success(null);
    }

    @ApiOperation(value = "审批")
    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    public QueryResponseResult approve(@LoginUser UserVO user,
                     @RequestBody FallBackVO vo) {

        return applyInfoService.approve(user,vo);
    }

    @ApiOperation(value = "加办")
    @PostMapping("/add")
    public QueryResponseResult add(@LoginUser UserVO user, @RequestBody ApproveVO approveVO) {
        return applyInfoService.add(user,approveVO);
    }

    @ApiOperation(value = "业务办理")
    @RequestMapping(value = "/impl", method = RequestMethod.POST)
    public QueryResponseResult impl(@LoginUser UserVO user,
                                    @ApiParam(value = "工单id",required = true) @RequestParam String id,
                                    @ApiParam(value = "驳回的环节id",required = true) @RequestParam String nodeId,
                                    @ApiParam(value = "当前环节id",required = true) @RequestParam String activityId,
                                    HttpServletRequest request) throws Exception {
        ApplyInfo info = applyInfoService.getById(id);
        String activityStr = workflowApi.getActivityById(activityId);
        WorkflowActivityVO activity = JSON.parseObject(workflowApi.getActivityById(activityId), WorkflowActivityVO.class);
//        HandlerWrapper hw = FormNum.getHandlerWrapperByInfo(context, info);
        HandlerWrapper hw=null;
        String json = IOUtils.toString(request.getInputStream(), "UTF-8");
//        ImplRequest implRequest = parseImplRequest(json, hw.getImplType());
        ImplRequest implRequest = new ImplRequest();
        if (implRequest.getResult() == null) {
            QueryResponseResult.fail("请选择实施结果！");
        }
        if (!"1".equals(implRequest.getResult())) {
            implRequest.setResult("0");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("info", info);
        param.put("implRequest", implRequest);

        String lockKey = info.getId().intern();
        String uuid = UUIDUtil.getUUID();
        try {
            if (lock.lock(lockKey, uuid)) {
                //服务审核信息表 自动订购 实施表
//                applyInfoService.saveImpl(user, param, hw.getImplHandler(),activity.getNodeId());
//                if ("1".equals(implRequest.getResult())&& hw.getImplHandler() instanceof IIaasYzmyzyUserService) {
//                    ImplRequest<IaasYzmyzyUser> implreq = implRequest;
//                    List<IaasYzmyzyUser> impls = implreq.getImplServerList();
//                    iaasYzmyzyUserImplService.saveUserList(impls,info.getAppName());
//                }
//                //防火墙零时开通时间设置
//                if("1".equals(implRequest.getResult()) && org.apache.commons.lang.StringUtils.equals(FormNum.PAAS_FIREWALL_OPEN.toString(),info.getFormNum())){
//                    PaasFirewallOpen firewall = paasFirewallOpenService.getOne(new QueryWrapper<PaasFirewallOpen>().lambda().eq(PaasFirewallOpen::getAppInfoId,id));
//                    if(firewall != null){
//                        if(firewall.getOpeningHours() == 1){
//                            logger.debug("防火墙零时开通");
//                            LocalDateTime now = LocalDateTime.now();
//                            LocalDateTime endDateTime = now.plusMonths(3);
//                            try{
//                                Date startTime = localDateTime2Date(now);
//                                Date endTime = localDateTime2Date(endDateTime);
//                                paasFirewallOpenService.update(new PaasFirewallOpen(),new UpdateWrapper<PaasFirewallOpen>().lambda()
//                                        .eq(PaasFirewallOpen::getAppInfoId,id)
//                                        .set(PaasFirewallOpen::getStartTime,startTime)
//                                        .set(PaasFirewallOpen::getEndTime,endTime));
//                            }catch (Exception e){
//                                logger.error("Fire Wall Time Set fail!");
//                            }
//                        }
//                    }
//                }
            } else {
                return QueryResponseResult.fail("服务器繁忙，请稍后重试");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            lock.unlock(lockKey, uuid);
        }
        if ("1".equals(implRequest.getResult())){
            workflowApi.advanceActivity(activityStr,null);
        }else {
            if (nodeId==null||nodeId.trim().equals("")) {
                return QueryResponseResult.fail("回退环节ID不能为空, 回退失败");
            }

            Map<String,String> map = new HashMap<>();
            map.put("name",info.getServiceTypeName());
            map.put("order",info.getOrderNumber());
            FallBackVO vo = new FallBackVO();
            vo.setCurrentActivityId(activityId);
            vo.setFallBackModelIds(nodeId);

            //查询回退环节id是否为业务办理，如果是，则走回退情况
            //如果只有一个环节，则直接回退到重新申请
            WorkflowNodeVO thisModel = JSON.parseObject(workflowApi.getNodeById(nodeId), WorkflowNodeVO.class);
            if(thisModel!=null&& WorkflowUtil.compareNodeAbility(thisModel.getNodeFeature(), WorkflowNodeAbilityType.IMPL.getCode())){
                //找到申请环节，并回退
                String firstNodeStr=workflowApi.getWorkflowNodeByParams(thisModel.getVersion(), thisModel.getWorkflowId(), 1);

                if(firstNodeStr!=null){
                    WorkflowNodeVO firstNode=JSON.parseObject(firstNodeStr, WorkflowNodeVO.class);
                    vo.setFallBackModelIds(firstNode.getId());
                }
                workflowApi.fallbackOnApproveNotPass(vo, map);
                info.setStatus(ApplyInfoStatus.IMPL_REJECT.getCode());
                applyInfoService.updateById(info);
            }else{
                workflowApi.fallbackOnApproveNotPass(vo, map);
            }
        }

        return QueryResponseResult.success(null);
    }
}

