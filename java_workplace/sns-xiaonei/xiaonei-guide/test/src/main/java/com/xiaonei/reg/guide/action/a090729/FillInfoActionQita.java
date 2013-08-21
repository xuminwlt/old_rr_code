/**
 * 
 */
package com.xiaonei.reg.guide.action.a090729;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.dodoyo.opt.DodoyoResource;
import com.xiaonei.antispam.model.CheckResult;
import com.xiaonei.platform.core.model.User;
import com.xiaonei.platform.core.opt.base.BaseThreadUtil;
import com.xiaonei.platform.core.opt.base.chain.impl.struts.util.BaseActionUtil;
import com.xiaonei.platform.core.opt.permission.define.Stage;
import com.xiaonei.reg.common.constants.Globals;
import com.xiaonei.reg.guide.action.base.GuideFlowBaseAction;
import com.xiaonei.reg.guide.form.f090729.FormQita;
import com.xiaonei.reg.guide.logic.StepStatusLogic;
import com.xiaonei.reg.guide.logic.fillinfo.GuideUserProfileLogic;
import com.xiaonei.reg.guide.util.GuideLogger;
import com.xiaonei.reg.register.logic.pretreatment.validate.Antispam;
import com.xiaonei.reg.register.logic.pretreatment.validate.CheckUniversity;

/**
 * 无阶段的fillinfo
 * 
 * @author wangtai
 * 
 */
public class FillInfoActionQita extends GuideFlowBaseAction {
	// public class NoStageFillInfoAction extends BaseAnonymousAction {

	private static final String FORWARD_SUCC = "succ";

	/**
	 * 成功后跳转
	 * 
	 * @param request
	 * @param form
	 * @param mapping
	 * @param response
	 * 
	 * @param host
	 * 
	 * @return
	 */
	@Override
	protected ActionForward succ(HttpServletRequest request, ActionForm form,
			ActionMapping mapping, HttpServletResponse response) {
		String rt = check(form, request);
		if (rt != null) {
			return err(rt, request, mapping, response);
		}
		rt = sb(request, form, mapping, response);
		if (rt != null) {
			return err(rt, request, mapping, response);
		}
		return null;
	}

	/**
	 * 出错流程
	 * 
	 * @param msg
	 * @param request
	 * @param mapping
	 * @param response
	 * @return
	 */
	private ActionForward err(String msg, HttpServletRequest request,
			ActionMapping mapping, HttpServletResponse response) {
		BaseActionUtil.addErr(request, msg);
		return initPage(request, mapping);
	}

	/**
	 * 检查用户提交数据
	 * 
	 * @param form
	 * @param request
	 * 
	 * @return
	 */
	private String check(ActionForm form, HttpServletRequest request) {
		GuideLogger.writeLine(this, "check() - start");
		MyTimeCost cost = MyTimeCost.begin();
		FormQita fform = (FormQita) form;
		cost.endStep();
		User host = BaseThreadUtil.currentHost(request);
		cost.endStep();
		String rt = null;
		cost.endStep();
		if(fform.getHomecitycode()==0){
			rt = "请选择所在地";
		}
		cost.endStep();

		List<String> antispamList = new ArrayList<String>();
		antispamList.add(fform.getHomeprovince());
		antispamList.add(fform.getUniversityname());
		cost.endStep();
		for (String antispamStr : antispamList) {
			if (StringUtils.isEmpty(antispamStr))
				continue;
			CheckResult cr = Antispam.checkAndFilterCR(host.getId(),
					antispamStr);
			switch (cr.getFlag()) {
			case CheckResult.SAFE:
				break;
			case CheckResult.PROHIBITED:
				return DodoyoResource.getString("errors.anti.web");
			default:
				break;
			}
		}
		cost.endStep();
		request.setAttribute("err_msg", rt);
		GuideLogger.writeLine(this, "check() - end"+rt);
		cost.endStep();
		cost.endFinally();
		return rt;
	}

	/**
	 * 处理用户提交数据
	 * 
	 * @param response
	 * @param mapping
	 * @param request
	 * @param form
	 * 
	 * @return
	 */
	private String sb(HttpServletRequest request, ActionForm form,
			ActionMapping mapping, HttpServletResponse response) {
		MyTimeCost cost = MyTimeCost.begin();
		request.setAttribute("action_form", form);
		cost.endStep();
		FormQita fform = (FormQita) form;
		User host = BaseThreadUtil.currentHost(request);
		GuideLogger.printLog(" host:"+host.getId()+MyRequestUtil.getBrowserInfo(request));
		cost.endStep();

		//大学
		cost.endStep();
		try {
			GuideLogger.writeLine(this , "sb() - hostid:"+ host.getId()+ " - saveuniv - univ:"+fform.getUniversityname());
			//大学
			String chk = new CheckUniversity().checkUniversity("1", fform.getUniversitycode(), fform.getUniversityname());
			if(StringUtils.equals(chk, "OK") || StringUtils.equals(chk, "请填写大学（选填）")){
				GuideUserProfileLogic.getInstance().saveUniversity(host, fform);
				//UserProfileUtil.saveUniversity(host, fform);
			}
		} catch (Exception e) {
			GuideLogger.writeLine(this, "sb() saveUniv() error "+host.getId()+" "+e.toString(),1);
		}
		
		//地理
		GuideUserProfileLogic.getInstance().saveRegionAndNet(host, fform);
		cost.endStep();
//		try {
//			MyLogger.writeLine(this , "sb() - hostid:"+ host.getId()+ " - savecity - city:"+fform.getHomecityname());
//			//地理
//			if(checkCityNet(fform)){ 
//				UserProfileUtil.saveRegionAndNet(host, fform);
//			}
//		} catch (Exception e) {
//			MyLogger.writeLine(this, "sb() saveCity() error "+host.getId()+" "+e.toString(),1);
//		}
		
		GuideLogger.writeLine(this , "sb() - hostid:"+ host.getId()+ " - savestage - stage:"+Stage.STAGE_NONE);
		//阶段
		GuideUserProfileLogic.getInstance().saveStage(host, Stage.STAGE_NONE);
		cost.endStep();
		
		cost.endFinally();
		return null;
	}

	
	@Override
	protected ActionForward initPage(HttpServletRequest request,
			ActionMapping mapping) {
		return mapping.findForward(FORWARD_SUCC);
	}

	@Override
	protected boolean shouldInit(HttpServletRequest request) {
		return !"post".equalsIgnoreCase(request.getMethod());
	}

	@Override
	protected String thisUrl() {
		return Globals.urlGuide + "/fi-090729.do";
	}

	@Override
	protected int thisStatus() {
		return StepStatusLogic.STEP_FILLINFO;
	}

	@Override
	protected String nextUrl() {
		return Globals.urlGuide + "/pv-090729.do";
	}
	
	@Override
	protected String nextUrl(HttpServletRequest request) {
		MyTimeCost cost = MyTimeCost.begin();
		ActionForm form = (ActionForm)request.getAttribute("action_form");
		cost.endStep();
		boolean has = false;
		cost.endStep();
		try {
			FormQita fform = (FormQita)form;
			cost.endStep();
			if(fform.getUniversitycode()!=0){
				has = true;
				cost.endStep();
			}

			cost.endStep();
		} catch (Exception e) {
			GuideLogger.writeLine(this,"nextUrl()",1);
		}
		cost.endFinally();
		String h = "false";
		if(has) h = "true";
		return Globals.urlGuide + "/pv-090729.do?h="+h;
	}
}