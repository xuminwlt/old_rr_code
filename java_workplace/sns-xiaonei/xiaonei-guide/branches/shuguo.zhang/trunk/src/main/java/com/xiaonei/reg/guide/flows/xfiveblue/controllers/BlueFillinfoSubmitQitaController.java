package com.xiaonei.reg.guide.flows.xfiveblue.controllers;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.annotation.rest.Post;

import com.xiaonei.commons.interceptors.access.loginrequired.LoginRequired;
import com.xiaonei.platform.core.model.User;
import com.xiaonei.platform.core.opt.permission.define.Stage;
import com.xiaonei.reg.guide.flows.xfive.formbeans.FormQita;
import com.xiaonei.reg.guide.flows.xfive.logics.GuideXFiveStepCheckLogic;
import com.xiaonei.reg.guide.flows.xfive.logics.GuideXFiveUserDecisionLogic;
import com.xiaonei.reg.guide.flows.xfive.utils.GuideXFiveRequestUtil;
import com.xiaonei.reg.guide.logic.FillInfoLogic;
import com.xiaonei.reg.guide.logic.StepStatusLogic;
import com.xiaonei.reg.guide.util.GuideLogger;
import com.xiaonei.reg.guide.util.GuideTimeCost;


@LoginRequired
@Path("fiblues-qt")
public class BlueFillinfoSubmitQitaController {
	private final static String INIT = "f:fiblue";
	private final int thisStep = StepStatusLogic.STEP_FILLINFO | StepStatusLogic.STEP_UPLOAD_HEAD | StepStatusLogic.STEP_PREVIEW;
	
	
	@Get    
	public String get(Invocation inv){
		return INIT;
	}
	
	public final String SUCC = "f:lead";
	
	@Post
	public String post(Invocation inv,@Param("formQita")FormQita form){
		
		String ret = INIT;
		HttpServletRequest request = GuideXFiveRequestUtil.getRequest(inv);
		final User host = GuideXFiveRequestUtil.getCurrentHost(request);
		
		if(request == null){
			GuideLogger.printLog(" request null ",GuideLogger.ERROR);
		}
		else if(host == null){
			GuideLogger.printLog(" host null ",GuideLogger.ERROR);
		}
		else{
			boolean isdone = GuideXFiveStepCheckLogic.getInstance().isTheStepDone(thisStep, host);
			if(isdone){
				ret = SUCC;
			}
			else{
				//String cr = checkAntiSpam();
				GuideTimeCost cost = GuideTimeCost.begin();
				
				try {
					//阶段
					//写阶段很关键 要同步
					GuideLogger.printLog("saveStage: "+host.getId()+" stage:"+Stage.STAGE_NONE);
					FillInfoLogic.getInstance().saveStage(host, Stage.STAGE_NONE);
					cost.endStep();
				} catch (Exception e) {
					GuideLogger.printLog(" host:"+host.getId()+" err:"+e.toString());
					e.printStackTrace();
				}
				
				try {
					//改状态
					GuideXFiveUserDecisionLogic.getInstance().saveDecision(host.getId(), GuideXFiveUserDecisionLogic.SOCIAL_GRAPH_BAD);
					//改标志
					StepStatusLogic.updateStepStatus(host.getId(), thisStep);
					GuideLogger.printLog("| host:"+host.getId()+" update step:"+thisStep);
					cost.endStep();
					cost.endFinally();
					ret = SUCC;
				} catch (Exception e) {
					GuideLogger.printLog(" host:"+host.getId()+" err:"+e.toString());
					e.printStackTrace();
				}
			}
			
		}
		return ret;
	}
}
