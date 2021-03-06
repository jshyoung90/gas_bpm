package com.hncis.familyJob.manager.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.hncis.common.application.ApprovalGasc;
import com.hncis.common.application.SessionInfo;
import com.hncis.common.dao.CommonJobDao;
import com.hncis.common.exception.impl.SessionException;
import com.hncis.common.message.HncisMessageSource;
import com.hncis.common.util.BpmApiUtil;
import com.hncis.common.util.FileUtil;
import com.hncis.common.util.StringUtil;
import com.hncis.common.vo.BgabGascZ011Dto;
import com.hncis.common.vo.BgabGascz002Dto;
import com.hncis.common.vo.CommonApproval;
import com.hncis.common.vo.CommonMessage;
import com.hncis.familyJob.dao.FamilyJobDao;
import com.hncis.familyJob.manager.FamilyJobManager;
import com.hncis.familyJob.vo.BgabGascfj01Dto;
import com.hncis.familyJob.vo.BgabGascfj02Dto;

@Service("familyJobManagerImpl")
public class FamilyJobManagerImpl  implements FamilyJobManager{
    private transient Log logger = LogFactory.getLog(getClass());

	@Autowired
	public FamilyJobDao familyJobDao;
	
	@Autowired
	public CommonJobDao commonJobDao;
	
	public List<BgabGascfj02Dto> selectGbListToFamilyJob(BgabGascfj02Dto vo){
		return familyJobDao.selectGbListToFamilyJob(vo);
	}
	
	public int saveRcToGbListToFamilyJob(List<BgabGascfj02Dto> list){
		int cnt = 0;
		for(BgabGascfj02Dto vo : list){
			if("".equals(vo.getF_seq())){
				cnt = familyJobDao.insertGbListToFamilyJob(vo);
			}else{
				cnt = familyJobDao.updateGbListToFamilyJob(vo);
			}
		}
		return cnt;	
	}

	public int deleteRcToGbListToFamilyJob(List<BgabGascfj02Dto> list){
		familyJobDao.deleteGbListToFamilyJobDetail(list);
		return familyJobDao.deleteGbListToFamilyJob(list);
	}
	
	public List<BgabGascfj02Dto> selectRelListToFamilyJob(BgabGascfj02Dto vo){
		return familyJobDao.selectRelListToFamilyJob(vo);
	}

	public int saveRcToRelListToFamilyJob(List<BgabGascfj02Dto> list){
		int cnt = 0;
		for(BgabGascfj02Dto vo : list){
			if("".equals(vo.getR_seq())){
				cnt = familyJobDao.insertRelListToFamilyJob(vo);
			}else{
				cnt = familyJobDao.updateRelListToFamilyJob(vo);
			}
		}
		return cnt;	
	}

	public int deleteRcToRelListToFamilyJob(List<BgabGascfj02Dto> list){
		return familyJobDao.deleteRelListToFamilyJob(list);
	}

	public List<BgabGascfj02Dto> selectToFamilyJobCombo(BgabGascfj02Dto vo){
		return familyJobDao.selectToFamilyJobCombo(vo);
	}

	public List<BgabGascfj02Dto> selectToFamilyJobCombo2(BgabGascfj02Dto vo){
		return familyJobDao.selectToFamilyJobCombo2(vo);
	}

	public int saveToFamilyJob(BgabGascfj01Dto vo){
		
		int cnt = 0;
		if("".equals(vo.getHid_doc_no())){
			cnt = familyJobDao.insertToFamilyJob(vo);
			
			if(cnt>0){
				// BPM API호출
				String processCode = "P-B-006"; 	//프로세스 코드 (경조사  프로세스) - 프로세스 정의서 참조
				String bizKey = vo.getDoc_no();	//신청서 번호
				String statusCode = "GASBZ01260010";	//액티비티 코드 (경조사신청서작성) - 프로세스 정의서 참조
				String loginUserId = vo.getEeno();	//로그인 사용자 아이디
				String comment = null;
				String roleCode = "GASROLE01260030";   //경조사 담당자 역할코드
				//역할정보
				List<String> approveList = new ArrayList<String>();
				List<String> managerList = new ArrayList<String>();
				managerList.add("10000001");

				BpmApiUtil.sendSaveTask(processCode, bizKey, statusCode, loginUserId, roleCode, approveList, managerList );
				
			}
			
		}else{
			cnt = familyJobDao.updateToFamilyJob(vo);
		}
		
		return cnt;	
	}
	
	public int deleteToFamilyJob(BgabGascfj01Dto vo){
		return familyJobDao.deleteToFamilyJob(vo);
	}
	
	public BgabGascfj01Dto selectToFamilyJob(BgabGascfj01Dto vo){
		BgabGascfj01Dto info = familyJobDao.selectToFamilyJob(vo); 
		info.setFj_compay(StringUtil.formatComma(StringUtil.isNullToString(info.getFj_compay(),"0")));
		return info;
	}
	
	public int selectToFamilyJobListCount(BgabGascfj01Dto vo){
		return familyJobDao.selectToFamilyJobListCount(vo);
	}
	
	public List<BgabGascfj01Dto> selectToFamilyJobList(BgabGascfj01Dto vo){
		return familyJobDao.selectToFamilyJobList(vo);
	}
	

	public void saveFjToFile(HttpServletRequest req, HttpServletResponse res, BgabGascZ011Dto bgabGascZ011Dto){
		String msg        = "";
		String orgNm 	= "";
		String resultUrl  = "xfj01_file.gas";
		String[] result   = new String[4];
		String[] paramVal = new String[4];
		
		try{
			paramVal[0] = "file_name";
			paramVal[1] = "old_file_name";
			paramVal[2] = "familyJob";
			
			result = FileUtil.uploadFileView(req, res, paramVal);
			
			if(result != null){
				if(result[0] != null){
					bgabGascZ011Dto.setOgc_fil_nm(result[0]);
					bgabGascZ011Dto.setFil_nm(result[5]);
					bgabGascZ011Dto.setFil_mgn_qty(Integer.parseInt(result[3]));
					Integer fileRs = (Integer)familyJobDao.insertFjToFile(bgabGascZ011Dto);
				}
				msg = result[4];
				orgNm = result[5];
			}else{
				resultUrl = "xfj01_file.gas";
				msg = HncisMessageSource.getMessage("FILE.0001");
			}
		}catch(Exception e){
			resultUrl = "xfj01_file.gas";
			msg = HncisMessageSource.getMessage("FILE.0001");
			e.printStackTrace();
		}finally{
			try{
				String dispatcherYN = "Y";
				req.setAttribute("hid_doc_no",  bgabGascZ011Dto.getDoc_no());
				req.setAttribute("hid_eeno",  bgabGascZ011Dto.getEeno());
				req.setAttribute("hid_pgs_st_cd",  bgabGascZ011Dto.getPgs_st_cd());
				req.setAttribute("hid_seq",  bgabGascZ011Dto.getSeq());
				req.setAttribute("dispatcherYN", dispatcherYN);
				req.setAttribute("csrfToken", bgabGascZ011Dto.getCsrfToken());
				req.setAttribute("message",  msg);
				req.setAttribute("saveYn",  "Y");
				req.setAttribute("orgNm", orgNm);
				req.getRequestDispatcher(resultUrl).forward(req, res);
			
				return;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public List<BgabGascZ011Dto> getSelectFjToFile(BgabGascZ011Dto bgabGascZ011Dto){
		return familyJobDao.getSelectFjToFile(bgabGascZ011Dto);
	}
	
	public BgabGascZ011Dto getSelectFjToFileInfo(BgabGascZ011Dto bgabGascZ011Dto){
		return familyJobDao.getSelectFjToFileInfo(bgabGascZ011Dto);
	}
	
	public int deleteFjToFile(List<BgabGascZ011Dto> bgabGascZ011IList){
		String fileResult = "";
		for(int i=0; i<bgabGascZ011IList.size(); i++){
			BgabGascZ011Dto fileInfo = bgabGascZ011IList.get(i);
			try {
				fileResult = FileUtil.deleteFile(fileInfo.getCorp_cd(), fileInfo.getCorp_cd() + "/familyJob", fileInfo.getOgc_fil_nm());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Integer fileDRs = (Integer)familyJobDao.deleteFjToFile(bgabGascZ011IList);
		return fileDRs;
	}
	
	@Override
	public CommonMessage updateFjToRequestForApprove(BgabGascfj01Dto dto, CommonApproval appInfo, CommonMessage message, HttpServletRequest req) {
		BgabGascz002Dto userParam = new BgabGascz002Dto();
		userParam.setCorp_cd(dto.getCorp_cd());
		userParam.setXusr_empno(dto.getEeno());
		BgabGascz002Dto userInfo = commonJobDao.getXusrInfo(userParam);
		
		appInfo.setDoc_no(dto.getDoc_no());					// 문서번호
		appInfo.setSystem_code("FJ");								// 시스템코드
		appInfo.setTable_name("bgab_gascfj01");						// 업무 테이블이름
		appInfo.setRpts_eeno(userInfo.getXusr_empno());		// 상신자 사번
		appInfo.setRpts_dept(userInfo.getXusr_dept_code());	// 상신자 부서코드
		appInfo.setStep_code(userInfo.getXusr_step_code());	// 상신자 직위코드
		appInfo.setRpts_eeno_nm(userInfo.getXusr_name());		// 상신자 성명
		appInfo.setStep_nm(userInfo.getXusr_step_name());		// 직위 이름
		appInfo.setTitle_nm(HncisMessageSource.getMessage("familyJob"));								// 업무 이름
		appInfo.setAppType("FJ");									// 전결권자 업무
		appInfo.setMax_level(5);									// 해외 결재 LEVEL
		appInfo.setCorp_cd(userInfo.getCorp_cd());

		CommonApproval commonApproval = ApprovalGasc.setApprovalRequestUseYn(appInfo);

		dto.setIf_id(commonApproval.getIf_id());
		dto.setRpts_eeno(userInfo.getXusr_empno());

		if(commonApproval.getResult().equals("Z")){
			message.setMessage(HncisMessageSource.getMessage("REQUEST.0000"));
			
			// BPM API호출
			String processCode = "P-B-006"; 	//프로세스 코드 (경조사 프로세스) - 프로세스 정의서 참조
			String bizKey = dto.getDoc_no();	//신청서 번호
			String statusCode = "GASBZ01260010";	//액티비티 코드 (경조사신청서작성) - 프로세스 정의서 참조
			String loginUserId = dto.getEeno();	//로그인 사용자 아이디
			String comment = null;
			String roleCode = "GASROLE01260030";   //경조사 담당자 역할코드
			
			//역할정보
			List<String> approveList = commonApproval.getApproveList();
			List<String> managerList = new ArrayList<String>();
			managerList.add("10000001");

			BpmApiUtil.sendCompleteTask(processCode, bizKey, statusCode, loginUserId, roleCode, approveList, managerList);
			
			
		}else{
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			message.setMessage(HncisMessageSource.getMessage("REQUEST.0001"));
			message.setErrorCode("E");
			message.setCode("");
			message.setCode1("");
		}

		return message;
	}

	@Override
	public CommonMessage updateFjToRequestForApproveCancel(BgabGascfj01Dto dto) {
		CommonMessage message = new CommonMessage();
		try{
			int cnt = familyJobDao.updatefJToRequestForApproveCancel(dto);
			message.setMessage(HncisMessageSource.getMessage("APPROVE.0002"));
			message.setCode1("Y");
			
			// BPM API호출
			String processCode = "P-B-006"; 	//프로세스 코드 (경조사  프로세스) - 프로세스 정의서 참조
			String bizKey = dto.getDoc_no();	//신청서 번호
			String statusCode = "GASBZ01260010";	//액티비티 코드 (경조사신청서작성) - 프로세스 정의서 참조
			String loginUserId = dto.getUpdr_eeno();	//로그인 사용자 아이디
			String comment = null;
			String roleCode = "GASROLE01260030";   //경조사 담당자 역할코드
			//역할정보
			List<String> approveList = new ArrayList<String>();
			List<String> managerList = new ArrayList<String>();
			managerList.add("10000001");
			
			BpmApiUtil.sendCollectTask(processCode, bizKey, statusCode, loginUserId, roleCode, approveList, managerList );

		}catch (Exception e) {
			message.setMessage(HncisMessageSource.getMessage("APPROVE.0003"));
			message.setCode1("N");
		}
		return message;
	}
	
	@Override
	public CommonMessage setApprovalCancel(BgabGascfj01Dto keyParamDto, CommonApproval appInfo, CommonMessage message, HttpServletRequest req) throws SessionException{
		appInfo.setIf_id(keyParamDto.getIf_id());
		appInfo.setTable_name("bgab_gascfj01");
		appInfo.setCorp_cd(SessionInfo.getSess_corp_cd(req));
		
		CommonApproval commonApproval = ApprovalGasc.setApprovalCancelProcess(appInfo);

		if(commonApproval.getResult().equals("Z")){
//			updateInfoTXToApprove(keyParamDto);
			// BPM API호출
			String processCode = "P-B-006"; 			//프로세스 코드 (경조사  프로세스) - 프로세스 정의서 참조
			String bizKey = keyParamDto.getDoc_no();	//신청서 번호
			String statusCode = "GASBZ01260010";		//액티비티 코드 (경조사신청서작성) - 프로세스 정의서 참조
			String loginUserId = keyParamDto.getUpdr_eeno();	//로그인 사용자 아이디
			String comment = null;
			String roleCode = "GASROLE01260030";   		//경조사 담당자 역할코드
			//역할정보
			List<String> approveList = new ArrayList<String>();
			List<String> managerList = new ArrayList<String>();
			managerList.add("10000001");
			
			BpmApiUtil.sendCollectTask(processCode, bizKey, statusCode, loginUserId, roleCode, approveList, managerList );

			message.setMessage(HncisMessageSource.getMessage("REQUEST.0002"));
		}else{
			message.setMessage(commonApproval.getMessage());
		}

		return message;
	}
	
	@Override
	public CommonMessage updateFjToRequestForConfirm(BgabGascfj01Dto dto) {
		CommonMessage message = new CommonMessage();
		try{
			int cnt = familyJobDao.updateFjToRequestForConfirm(dto);
			message.setMessage(HncisMessageSource.getMessage("CONFIRM.0000"));
			message.setCode1("Y");
			
			// BPM API호출
			String processCode = "P-B-006"; 	//프로세스 코드 (경조사 프로세스) - 프로세스 정의서 참조
			String bizKey = dto.getDoc_no();	//신청서 번호
			String statusCode = "GASBZ01260030";	//액티비티 코드 (경조사 담당자확인) - 프로세스 정의서 참조
			String loginUserId = dto.getUpdr_eeno();	//로그인 사용자 아이디
			String comment = null;
	
			BpmApiUtil.sendFinalCompleteTask(processCode, bizKey, statusCode, loginUserId);
						
		}catch (Exception e) {
			message.setMessage(HncisMessageSource.getMessage("CONFIRM.0001"));
			message.setCode1("N");
		}
		return message;
	}

	@Override
	public CommonMessage updateFjToRequestForReject(BgabGascfj01Dto dto) {
		CommonMessage message = new CommonMessage();
		try{
			int cnt = familyJobDao.updateFjToRequestForReject(dto);
			message.setMessage(HncisMessageSource.getMessage("REJECT.0000"));
			message.setCode1("Y");
			
			// BPM API호출
			String processCode = "P-B-006"; 	//프로세스 코드 (경조사 프로세스) - 프로세스 정의서 참조
			String bizKey = dto.getDoc_no();	//신청서 번호
			String statusCode = "GASBZ01260030";	//액티비티 코드 (경조사 담당자확인) - 프로세스 정의서 참조
			String loginUserId = dto.getUpdr_eeno();	//로그인 사용자 아이디
			
			BpmApiUtil.sendDeleteAndRejectTask(processCode, bizKey, statusCode, loginUserId);
							
		}catch (Exception e) {
			message.setMessage(HncisMessageSource.getMessage("REJECT.0001"));
			message.setCode1("N");
		}
		return message;
	}
}
