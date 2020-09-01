package com.hirisun.cloud.saas.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hirisun.cloud.api.workflow.WorkflowApi;
import com.hirisun.cloud.common.contains.ApplyInfoStatus;
import com.hirisun.cloud.common.vo.CommonCode;
import com.hirisun.cloud.model.saas.vo.SaasApplicationMergeVO;
import com.hirisun.cloud.model.workflow.WorkflowActivityVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hirisun.cloud.api.file.FileApi;
import com.hirisun.cloud.api.platform.UserDocApi;
import com.hirisun.cloud.api.system.FilesApi;
import com.hirisun.cloud.api.system.SmsApi;
import com.hirisun.cloud.api.user.UserApi;
import com.hirisun.cloud.common.constant.BusinessName;
import com.hirisun.cloud.common.contains.ApplicationInfoStatus;
import com.hirisun.cloud.common.exception.CustomException;
import com.hirisun.cloud.common.util.AreaPoliceCategoryUtils;
import com.hirisun.cloud.model.app.param.SubpageParam;
import com.hirisun.cloud.model.file.FilesVo;
import com.hirisun.cloud.model.impl.vo.ImplRequestVo;
import com.hirisun.cloud.model.platform.vo.UserDocVo;
import com.hirisun.cloud.model.saas.vo.SaasOrderTotalVo;
import com.hirisun.cloud.model.saas.vo.SaasTotalVo;
import com.hirisun.cloud.model.saas.vo.SaasUseTotalVo;
import com.hirisun.cloud.model.user.UserVO;
import com.hirisun.cloud.saas.bean.SaasApplication;
import com.hirisun.cloud.saas.bean.SaasApplicationMerge;
import com.hirisun.cloud.saas.handle.CommonHandler;
import com.hirisun.cloud.saas.mapper.SaasApplicationMergeMapper;
import com.hirisun.cloud.saas.service.ISaasApplicationMergeService;
import com.hirisun.cloud.saas.service.ISaasApplicationService;
import com.hirisun.cloud.saas.vo.SaasExceptionCode;

/**
 * <p>
 * SaaS资源申请合并信息表 服务实现类
 * </p>
 *
 * @author wuc
 * @since 2019-07-24
 */
@Service
public class SaasApplicationMergeServiceImpl extends ServiceImpl<SaasApplicationMergeMapper, 
	SaasApplicationMerge> implements ISaasApplicationMergeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ISaasApplicationService saasApplicationService;
    //TODO
//    @Autowired
//    private IActivityService activityService;
//    @Autowired
//    private IInstanceService instanceService;
//    @Autowired
//    private IWorkflowmodelService workflowmodelService;
//    @Autowired
//    private IWorkflowService workflowService;
//    @Autowired
//    private IFilesService filesService;
//    @Autowired
//  private ISpeedUpService speedUpService;
    @Autowired
    private FilesApi filesApi;

    @Autowired
    private WorkflowApi workflowApi;
//    @Autowired
//    private IAppReviewInfoService appReviewInfoService;
    @Autowired
    private UserApi userApi;
//    
    @Autowired
    private FileApi fileApi;
    @Autowired
    private UserDocApi userDocApi;
    @Autowired
    private SmsApi smsApi;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SaasApplicationMerge merge(String userId, String ids) {

        UserVO user = userApi.getUserByIdCard(userId);
    	//TODO
//        String[] idArray = ids.split(",");
//        SaasApplication application = saasApplicationService.getById(idArray[0]);
//        Workflow workflow = null;
//        //将可视化建模平台和通用应用流程区分开
//        if ("1".equals(application.getVisible())){
//            workflow = workflowService.getOne(new QueryWrapper<Workflow>().eq("DEFAULT_PROCESS","SAAS_VISIBLE"));
//        }else if ("2".equals(application.getVisible())){ //将广东公安数据接入平台和通用应用流程区分开
//            workflow = workflowService.getOne(new QueryWrapper<Workflow>().eq("DEFAULT_PROCESS","SAAS_DATA_ACCESS"));
//        }else {
//            workflow = workflowService.getOne(new QueryWrapper<Workflow>().eq("DEFAULT_PROCESS","SAAS"));
//        }
//        if (workflow==null){
//            throw new BaseException("未配置流程");
//        }
//        SaasApplicationMerge merge = new SaasApplicationMerge();
//        merge.setCreator(user.getIdcard());
//        merge.setCreatorName(user.getName());
//        merge.setOrgId(user.getOrgId());
//        merge.setOrgName(user.getOrgName());
//        merge.setPostType(user.getPostType());
//        merge.setMobileWork(user.getMobileWork());
//        merge.setApplicationTime(new Date());
//        merge.setStatus(ApplicationInfoStatus.INNER_REVIEW.getCode());
//        merge.setWorkFlowId(workflow.getId());
//        merge.setWorkFlowVersion(workflow.getVersion());
//        String orderNum = OrderNum.gen(stringRedisTemplate, RedisKey.KEY_ORDER_NUM_PREFIX);
//        merge.setOrderNumber(orderNum);
//        merge.setAreas(user.getTenantArea());
//        merge.setPoliceCategory(user.getTenantPoliceCategory());
//        this.save(merge);
//        // 更新原始单据为已关联
//        List<SaasApplication> list = new ArrayList<>(idArray.length);
//        for (String id : idArray) {
//            SaasApplication saasApplication = new SaasApplication();
//            saasApplication.setId(id);
//            saasApplication.setMergeId(merge.getId());
//            list.add(saasApplication);
//        }
//        saasApplicationService.updateBatchById(list);
//
//        R r= instanceService.launchInstanceOfWorkFlow(user.getIdcard(), merge.getWorkFlowId(), merge.getId());
//
//        Workflowmodel workflowmodel = workflowmodelService.getOne(new QueryWrapper<Workflowmodel>()
//                .eq("WORKFLOWID",workflow.getId()).eq("modelname", ModelName.LVL2_MANAGER.getName()).eq("VERSION",workflow.getVersion()));
//
//        Map<String, String> modelMapToPerson = new HashMap<>();
//        modelMapToPerson.put(workflowmodel.getId(), user.getIdcard());
//        AdvanceBeanVO advanceBeanVO = new AdvanceBeanVO();
//        advanceBeanVO.setCurrentActivityId(r.get("data").toString());
//        advanceBeanVO.setModelMapToPerson(modelMapToPerson);
//        Map<String,String> map = new HashMap<>();
//        map.put("name", BusinessName.SAAS_RESOURCE);
//        map.put("order", merge.getOrderNumber());
//        activityService.advanceCurrentActivity(advanceBeanVO, map);
//    	  smsApi.buildSuccessMessage(user.getIdcard(), BusinessName.SAAS_RESOURCE, merge.getOrderNumber());
    	return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(String userId, SaasApplicationMerge info) {
        info.setStatus(null);
        info.setOrderNumber(null);
        info.setWorkFlowId(null);
        this.updateById(info);
        // 关联文件信息
//        filesService.refFiles(info.getFileList(), info.getId());
        SubpageParam param = new SubpageParam();
        param.setFiles(info.getFileList());
        param.setRefId(info.getId());
		filesApi.refFiles(param);
    }

    @Override
    public SaasApplicationMerge getDetails(String id) {
        SaasApplicationMerge info = this.getById(id);
        if (null == info) {
            return null;
        }

        UserVO userVO = userApi.getUserByIdCard(info.getCreator());
        info.setUser(userVO);
        info.setApplicationList(saasApplicationService.getListByMergeId(id));
        
        SubpageParam param = new SubpageParam();
        param.setRefId(id);
		List<FilesVo> fileList = filesApi.find(param);
        
		info.setFileList(fileList);
        // 审核信息
		// TODO
//        List<AppReviewInfo> allReviewInfo = appReviewInfoService.getAllReviewInfoByAppInfoId(id);
//        info.setReviewList(allReviewInfo);
//        // 实施审批信息
//        AppReviewInfo implInfo = null;
//        AppReviewInfo lastReviewInfo = appReviewInfoService.getLastPassReviewInfoByAppInfoId(id);
//        if (lastReviewInfo != null && "2".equals(lastReviewInfo.getrType())) {
//            // 最近一条审核记录为实施记录
//            implInfo = lastReviewInfo;
//            param.setRefId(implInfo.getId());
//    		fileList = filesApi.find(param);
//            implInfo.setFileList(fileList);
//            info.setImpl(implInfo);
//        }
        return info;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteById(String userId, String id) {
        SaasApplicationMerge info = this.getById(id);
        if (info == null) {
            throw new CustomException(SaasExceptionCode.RECORD_NULL_ERROR);
        }
        if (!Objects.equals(userId, info.getCreator())) {
            throw new CustomException(SaasExceptionCode.DELETE_ERROR);
        }
        saasApplicationService.update(new SaasApplication(), new UpdateWrapper<SaasApplication>().lambda()
                .eq(SaasApplication::getMergeId, id)
                .set(SaasApplication::getMergeId, null)
                .set(SaasApplication::getStatus, ApplicationInfoStatus.INNER_REVIEW.getCode()));
        info.deleteById();
    }


    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void saveImpl(UserVO user, Map<String, Object> param, String modelId) {
        SaasApplicationMerge info = (SaasApplicationMerge) param.get("info");
        // 添加实施信息
        ImplRequestVo implRequest = (ImplRequestVo) param.get("implRequest");
        String result = implRequest.getResult();
        String remark = implRequest.getRemark();
        //TODO
//        AppReviewInfo reviewInfo = new AppReviewInfo();
//        reviewInfo.setCreator(user.getIdcard());
//        reviewInfo.setResult(result);
//        reviewInfo.setRemark(remark);
//        reviewInfo.setrType("2");
//        reviewInfo.setStepName(ModelName.CARRY.getName());
//        reviewInfo.setFlowStepId(modelId);
//        reviewInfo.setAppInfoId(info.getId());
//        reviewInfo.insert();
//        // 实施附件
//        filesService.refFiles(implRequest.getFileList(), reviewInfo.getId());
//        
//        新改造的代码别删
//        SubpageParam abc = new SubpageParam();
//        abc.setFiles(implRequest.getFileList());
//        abc.setRefId(reviewInfo.getId());
//		filesApi.refFiles(abc );
        
        ApplicationInfoStatus status;
        if ("0".equals(result)) {
            // 驳回申请
            status = ApplicationInfoStatus.REVIEW;
        } else {
            // 实施步骤已完成,修改申请为使用状态
            status = ApplicationInfoStatus.USE;
            //通过合并后的id查找申请信息集合，然后依次给申请人发送短信
            List<SaasApplication> saasApplicationList = saasApplicationService.getListByMergeId(info.getId());
            for (SaasApplication saasApplication:saasApplicationList) {
            	smsApi.buildSuccessMessage(saasApplication.getCreator(), BusinessName.SAAS_RESOURCE, saasApplication.getOrderNumber());
            }
        }
        this.update(new SaasApplicationMerge(), new UpdateWrapper<SaasApplicationMerge>().lambda()
                .eq(SaasApplicationMerge::getId, info.getId())
                .set(SaasApplicationMerge::getStatus, status.getCode()));
        saasApplicationService.updateStatus(info.getId(), status.getCode());
    }

    @Override
    public Page<SaasApplicationMergeVO> getFlowPage(String userId, Map<String, Object> param) {
        Page<SaasApplicationMergeVO> page = new Page<>();
        page.setCurrent(Integer.parseInt(param.get("pageNum").toString()));
        page.setSize(Integer.parseInt(param.get("pageSize").toString()));
        UserVO user =JSON.parseObject(param.get("user").toString(), UserVO.class);
        param.put("user", user);
        page = baseMapper.getFlowPage(page, param);
        List<SaasApplicationMergeVO> records = page.getRecords();
        if (records != null && !records.isEmpty()) {
            curHandlerPerson(records, user);
        }
        return page;
    }

    @Override
    public Page<SaasApplicationMergeVO> getFlowPageWithServiceName(String userId, Map<String, Object> param) {
        Page<SaasApplicationMergeVO> page = new Page<>();
        page.setCurrent(Integer.parseInt(param.get("pageNum").toString()));
        page.setSize(Integer.parseInt(param.get("pageSize").toString()));
        UserVO user =JSON.parseObject(param.get("user").toString(), UserVO.class);
        param.put("user", user);
        page = baseMapper.getFlowPageWithServiceName(page, param);
        List<SaasApplicationMergeVO> records = page.getRecords();
        if (records != null && !records.isEmpty()) {
            curHandlerPerson(records, user);
        }
        return page;
    }

    @Override
    public List<SaasTotalVo> saasTotal(String areas, String policeCategory, Map<String, String> params) {
        List<SaasTotalVo> data = baseMapper.saasTotal(areas, policeCategory, params);
        for (int i = 0; i < data.size(); i++) {
            SaasTotalVo st = data.get(i);
            st.setId(i + 1);
            st.setApproveNum(st.getNum());
            st.setCarryNum(st.getNum());
            if (StringUtils.isNotEmpty(st.getAreas())) {
                st.setAreasPoliceCategory(st.getAreas());
            } else {
                st.setAreasPoliceCategory(st.getPoliceCategory());
            }
        }
        return data;
    }

    @Override
    public List<SaasOrderTotalVo> saasOrderTotal(String areas, String policeCategory, Map<String, String> params) {
        List<SaasOrderTotalVo> data = baseMapper.saasOrderTotal(areas, policeCategory, params);
        for (int i = 0; i < data.size(); i++) {
            SaasOrderTotalVo st = data.get(i);
            st.setId(i + 1);
            if ("0".equals(st.getApplyType())) {
                st.setAreasPoliceCategory(st.getAreas());
            } else {
                st.setAreasPoliceCategory(st.getPoliceCategory());
            }
        }
        return data;
    }

    @Override
    public List<SaasOrderTotalVo> saasOrderQuery(String areas, String policeCategory, Map<String, String> params) {
        List<SaasOrderTotalVo> data = baseMapper.saasOrderQuery(areas, policeCategory, params);
        for (int i = 0; i < data.size(); i++) {
            SaasOrderTotalVo st = data.get(i);
            st.setId(i + 1);
            if ("0".equals(st.getApplyType())) {
                st.setAreasPoliceCategory(st.getAreas());
            } else {
                st.setAreasPoliceCategory(st.getPoliceCategory());
            }
        }
        return data;
    }

    @Override
    public List<SaasUseTotalVo> saasUseTotal(String areas, String policeCategory, Map<String, String> params) {
        List<SaasUseTotalVo> data = baseMapper.saasUseTotal(areas, policeCategory, params);
        for (int i = 0; i < data.size(); i++) {
            SaasUseTotalVo st = data.get(i);
            st.setId(i + 1);
            if ("0".equals(st.getApplyType())) {
                st.setAreasPoliceCategory(st.getAreas());
            } else if(StringUtils.isNotBlank(st.getAreas()) && !"省厅".equals(st.getAreas())){ //地市不为空且不为省厅作为地市管理
                //属于二十一地市
                if(AreaPoliceCategoryUtils.isContainAreaName(CommonHandler.splitArea(st.getAreas()))){
                    st.setAreasPoliceCategory(st.getAreas());
                }
            }else {
                st.setAreasPoliceCategory(st.getPoliceCategory());
            }
            if ("-1".equals(st.getRecoverFlag())) {
                // 表示已经回收
                st.setStatus("否");
            } else {
                st.setStatus("是");
            }
        }
        return data;
    }

    @Override
    public void downloadFile(HttpServletResponse response) throws IOException {
        downFile(response, "建模平台用户开通申请总表.doc");
    }

    private void downFile(HttpServletResponse response, String name) throws IOException {
    	
    	List<UserDocVo> docVoList = userDocApi.find("建模平台用户开通申请总表");
    	if(CollectionUtils.isNotEmpty(docVoList)) {
    		UserDocVo userDocVo = docVoList.get(0);
    		String fileId = userDocVo.getFileId();
    		byte[] download = fileApi.download(fileId);
    		// 设置文件名
    		response.setContentType("application/force-download");
            String originaName;
            try {
                originaName = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                originaName = "建模平台用户开通申请总表.doc";
            }
            response.addHeader("Content-Disposition", "attachment;fileName=" + originaName);
            InputStream inputStream = new ByteArrayInputStream(download);
            IOUtils.copy(inputStream, response.getOutputStream());
    	}
    }

    /**
     *
     * @param applyInfoList
     * @param user
     */
    public  void curHandlerPerson(List<SaasApplicationMergeVO> applyInfoList, UserVO user){
        List<String> instanceId = applyInfoList.stream().map(SaasApplicationMergeVO::getInstanceId).distinct().collect(Collectors.toList());
        //实例Id到处理人身份证的集合(单表查询)
        Map<String,String> instance2IdCardsMap = workflowApi.instanceToHandleIdCards(instanceId);
        List<String> handlePeronIdsList = Lists.newArrayList();
        instance2IdCardsMap.forEach((k,v)->{
            if(StringUtils.isNotBlank(v)){
                handlePeronIdsList.add(v);
            }
        });
        //获取身份证号到名字的Map
        Map<String,String> idCard2NameMap = idCardsNameMap(handlePeronIdsList);
        //获取实例-待办处理人身份证号Map
        for(SaasApplicationMergeVO record:applyInfoList){
            if(instance2IdCardsMap.size()!=0){
                if(instance2IdCardsMap.containsKey(record.getInstanceId()) && instance2IdCardsMap.get(record.getInstanceId()) != null){
                    record.setProcessingPerson(instance2IdCardsMap.get(record.getInstanceId()));
                }
            }
        }
        for (SaasApplicationMergeVO record : applyInfoList) {
            if(idCard2NameMap != null){
                //身份证号集合字符串替换为名字集合字符串
                convertIdCardToName(idCard2NameMap,record);
            }
            ApplyInfoStatus applyInfoStatus = ApplyInfoStatus.codeOf(record.getStatus());
            // 判断是否能删除
            if (applyInfoStatus != ApplyInfoStatus.DELETE
                    && Objects.equals(user.getIdcard(), record.getCreator())) {
                record.setCanDelete(true);
            }
        }
    }
    /**
     * 获取身份证号与名字关联的Map
     * @param idCardsList 身份证号集合 {"5110022522,545451515","45454551515,454554"}
     * @return  {"5110022522":"jack"}
     */
    public Map<String,String> idCardsNameMap(List<String> idCardsList){
        List<String> idCardElementList = Lists.newArrayList();
        idCardsList.forEach(idCards->{
            if(StringUtils.isNotEmpty(idCards)){
                List<String> idCardList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(idCards);
                if(CollectionUtils.isNotEmpty(idCardList)){
                    idCardElementList.addAll(idCardList);
                }
            }
        });
        if(CollectionUtils.isNotEmpty(idCardElementList)){
            List<UserVO> userList = userApi.getUserByIdCardList(idCardElementList);
            if (CollectionUtils.isEmpty(userList)) {
                throw new CustomException(CommonCode.SERVER_ERROR);
            }
            return userList.stream().collect(Collectors.toMap(UserVO::getIdcard,UserVO::getName));
        }
        return null;
    }

    /**
     * 身份证号转名字
     * @param idCardToNameMap
     * @param record
     */
    public void convertIdCardToName(Map<String,String> idCardToNameMap,SaasApplicationMergeVO record){
        if(idCardToNameMap != null){
            if(StringUtils.isNotBlank(record.getProcessingPerson())){
                List<String> nameList = Lists.newArrayList();
                List<String> idCardList = Splitter.on(",")
                        .trimResults().omitEmptyStrings().splitToList(record.getProcessingPerson());

                idCardList.forEach(idCard->{
                    if(idCardToNameMap.containsKey(idCard)){
                        nameList.add(idCardToNameMap.get(idCard));
                    }
                });

                String names = Joiner.on(",").skipNulls().join(nameList);
                record.setProcessingPerson(names);
            }
        }
    }

}
