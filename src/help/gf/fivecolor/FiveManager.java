package help.gf.fivecolor;

import help.base.APIConstants;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.json.JSONArray;
import org.json.JSONObject;

import com.apps.Constants;
import com.apps.eff.CacheUtil;
import com.apps.model.Param;
import com.apps.util.JsonUtil;
import com.apps.util.ProductUtil;
import com.gf.fivecolor.FiveConstants;
import com.gf.fivecolor.model.GfFiveGaOmit;
import com.gf.fivecolor.model.GfFiveGaSession;
import com.gf.fivecolor.model.GfFiveGaTrend;
import com.gf.fivecolor.service.IFiveService;
import com.gf.fivecolor.util.FiveUtil;
import com.framework.dao.hibernate.PaginationSupport;
import com.framework.util.DateTimeUtil;
import com.framework.util.DesUtils;
import com.framework.util.ParamUtils;
import com.framework.util.StringUtil;
import com.framework.web.action.BaseDispatchAction;
import com.game.model.GaBetOption;
import com.game.model.GaSessionInfo;
import com.game.model.dto.WinCoDTO;
import com.game.service.IGaService;
import com.ram.RamConstants;
import com.ram.model.User;
import com.gf.ssc.cqssc.CqSscConstants;
import com.gf.ssc.cqssc.util.CqSscUtil;
import com.gf.three.util.ThreeUtil;

public class FiveManager extends BaseDispatchAction {
	private final IGaService gaService = (IGaService) getService("gaService");
	private final IFiveService gfFiveService = (IFiveService) getService("gfFiveService");
	
	/**
	 * 面板选项
	 * 
	 * @param mapping
	 */

	public void betPanel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		Map<String, String> deParameter = ParamUtils.deParameter(request);
		String playType = ParamUtils.getParameter(deParameter, "playType",FiveConstants.PLAY_TYPE_SIX);//默认是三星直选
		
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String message = "";
		String code = APIConstants.CODE_REQUEST_ERROR;
		List<GfFiveGaTrend> list = null;
		if (message.equals("")) {
			JSONArray items = new JSONArray();// 十位数据集合
			JSONObject obj = null;
			if("0".equals(playType)){ //玩法为大小单双
				String name = "大小单双";
				for(int i=0;i<4;i++){
					obj.put("ballName", name.charAt(i)); //球
					obj.put("trend", "0"); //最近开奖距离现在场次
					items.put(obj);
				}
				code = APIConstants.CODE_REQUEST_SUCCESS;
				message = "操作成功";
			}else{
				list = gfFiveService.findFcTrendList();
				if (list != null && list.size() > 0) {
					for (GfFiveGaTrend tr : list) {
						obj = new JSONObject();
						if(ParamUtils.chkString(tr.getTrendTitle())){
							obj.put("ballName", tr.getTrendTitle()); //球
							if(ParamUtils.chkInteger(tr.getTrendCount())){
							    obj.put("trend", tr.getTrendCount()); //最近开奖距离现在场次
							}else{
								obj.put("trend", 0); //最近开奖距离现在场次
							}
						}else{
							obj.put("ballName", ""); //球
							obj.put("trend", 0); //最近开奖距离现在场次
						}
						items.put(obj);
					}
					code = APIConstants.CODE_REQUEST_SUCCESS;
					message = "操作成功";
				} else {
					code = APIConstants.CODE_NOT_FOUND;
					message = "操作失败";
				}
			}
			data.put("items", items);
		}
		map.put("data", data);
		map.put("msg", message);
		map.put("code", code);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 彩票玩法
	 * 
	 * @param mapping
	 */
	public void gamePlayType(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String message = "";
		String code = APIConstants.CODE_REQUEST_ERROR;
		List<GaBetOption> opList = null;
	
		if (message.equals("")) {
			opList = gaService.findGaBetOptionByGameType(Constants.GAME_TYPE_GF_FC);
			log.info("-----opList.size()----"+opList.size());
			GaSessionInfo  gaSessionInfo= gaService.findGaSessionInfo(Constants.GAME_TYPE_GF_FC);//五分彩
			if(gaSessionInfo !=null){
				data.put("img", ParamUtils.chkStringNotNull(StringUtil.checkAPIHttpUrl(gaSessionInfo.getImg())));
				data.put("describe", gaSessionInfo.getDes());//描述
			}else{//找不到该游戏
				data.put("img", "");//图片地址
				data.put("describe", "");//描述
			}

			if(opList != null && opList.size() > 0){
				JSONArray barItem = new JSONArray();// 数据集合
				JSONObject barObj = null;
				for (GaBetOption op : opList) {
					barObj = new JSONObject();
					barObj.put("playType", op.getPlayType());//玩法类型
					barObj.put("playName", op.getTitle());//玩法名字
					barObj.put("betRate", op.getBetRate());//赔率
					barObj.put("pointMultiple",op.getPointMultiple());//积分倍率
					barItem.put(barObj);
					data.put("items", barItem);
				}
				code = APIConstants.CODE_REQUEST_SUCCESS;
				message = "操作成功";
			} else {
				code = APIConstants.CODE_NOT_FOUND;
				message = "操作失败";
			}
		}
		map.put("data", data);
		map.put("msg", message);
		map.put("code", code);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 开奖。
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void sessionInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {	
		Map<String, String> deParameter = ParamUtils.deParameter(request);
		String ucode = ParamUtils.getParameter(deParameter, "u");

		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层

		String code = APIConstants.CODE_REQUEST_ERROR;
		if(ParamUtils.chkString(ucode)){
			User user = null;
			Map<String, String> decryptMap = DesUtils.decryptMap(ucode);
			Integer uid = Integer.valueOf(decryptMap
					.get(Constants.DES_KEY_UID));
			user = (User) userService.getObject(User.class, uid);
			if (user == null) {
				data.put("money", "0");
				data.put("point", "0");
			}else{
				BigDecimal balance = user.getMoney();
				if (balance == null)
					balance = new BigDecimal(0);// 判空处理
				BigDecimal points = user.getUserpoints();
				if (points == null)
					points = new BigDecimal(0);// 判空处理

				data.put("money",balance.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
				data.put("point",points.setScale(0, BigDecimal.ROUND_HALF_UP).toString());
			}
		}else{
			data.put("money", "0");
			data.put("point", "0");
		}

		GfFiveGaSession session = gfFiveService.getCurrentSession();
		if (session != null) {
			long openTime = (session.getEndTime().getTime() - System
					.currentTimeMillis()) / 1000; // 转成秒
			long betTime = 0;
			if (openTime >= FiveConstants.FC_BET_END_INTERVAL) {
				betTime = openTime - FiveConstants.FC_BET_END_INTERVAL;
			}
			data.put("sessionNo", session.getSessionNo()); //当前期号 
			data.put("betTime", Long.toString(betTime));//现在距离封盘时间秒数
			data.put("openTime", Long.toString(openTime));//现在距离开奖时间秒数
			data.put("openDate",
					DateTimeUtil.DateToStringHHMM(session.getEndTime())); //开奖时间

			GfFiveGaSession preSession = gfFiveService.getPreviousSessionBySessionNo(String.valueOf(Integer.parseInt(session.getSessionNo())-1));
			if(preSession != null){
				String preSessionNo = preSession.getSessionNo();
				if(preSessionNo.length()>0){
					data.put("preSessionNo", preSessionNo);//上期期号
				}
				String openResult = preSession.getOpenResult();
				if(ParamUtils.chkString(openResult)){
					String array[] = preSession.getOpenResult().split(",|，|\\+|\\|");
					JSONArray jsonArray = new JSONArray();
					for (int j = 0; j < array.length; j++) {
						jsonArray.put(array[j]);
					}
					data.put("resultItems", jsonArray);//上期期号开奖结果
				}else{
					data.put("resultItems",  new JSONArray());//上期期号开奖结果
				}
			}else{
				preSession = (GfFiveGaSession) gfFiveService.getObject(GfFiveGaSession.class, session.getSessionId()-1);
				if(preSession != null){
					data.put("preSessionNo", preSession.getSessionNo());//上期期号
					String array[] = preSession.getOpenResult().split(",|，|\\+|\\|");
					JSONArray jsonArray = new JSONArray();
					for (int j = 0; j < array.length; j++) {
						jsonArray.put(array[j]);
					}
					data.put("resultItems", jsonArray);//上期期号开奖结果
				}else{
					data.put("preSessionNo", "");//上期期号
					data.put("resultItems",  new JSONArray());//上期期号开奖结果			
				}
			}
			List<Object> para = new ArrayList<Object>();
			StringBuffer hqls = new StringBuffer();

			hqls.append("and ho.sessionNo > ? ");
			para.add(session.getSessionNo());
			hqls.append(" order by ho.sessionNo asc");
			PaginationSupport ps = gfFiveService.findFcGaSessionList(hqls.toString(), para, 0, 50);
			List<GfFiveGaSession> list = ps.getItems();
			if(list !=null){
				JSONArray noArr = new JSONArray();
				for(int i=0;i<list.size();i++){
					GfFiveGaSession se = list.get(i);
					noArr.put(se.getSessionNo());
				}
				data.put("noArray", noArr);//50期期号
			}

			code = APIConstants.CODE_REQUEST_SUCCESS;
		}else{//查询不到当前期
			data.put("sessionNo", "");
			data.put("betTime", "");//现在距离封盘时间秒数
			data.put("openTime", "");//现在距离开奖时间秒数
			data.put("openDate",""); //开奖时间
			data.put("preSessionNo", "");//上期期号
			data.put("resultItems",  new JSONArray());//上期期号开奖结果			
		}

		map.put("data", data);
		map.put("code", code);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 下注
	 */
	public void bet(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);

		String betBallArray = ParamUtils.getParameter(deParameter,
			    "betBallArray", "");// [{t:1,b:12,13,16,17,19|2}] 投注的球
		String sessionArray = ParamUtils.getParameter(deParameter,
				"sessionArray", "");// [{s:12,m:2}] 期号，倍数
		
		String betType = ParamUtils.getParameter(deParameter, "betType"); //购买类型 0=代购1=合买
		String isAddNo = ParamUtils.getParameter(deParameter, "isAddNo"); //是否追号--代购
		String isWinStop = ParamUtils.getParameter(deParameter, "isWinStop"); //是否中奖后停止---代购
		String num = ParamUtils.getParameter(deParameter, "num"); //总份数  ---合买
		String buyNum = ParamUtils.getParameter(deParameter, "buyNum"); //购买份数---合买
		String isGuaranteed =  ParamUtils.getParameter(deParameter, "isGuaranteed"); //是否保底---合买
		String guaranteedNum =  ParamUtils.getParameter(deParameter, "guaranteedNum"); //保底份数---合买
		String ucode = ParamUtils.getParameter(deParameter, "u");

		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String code = APIConstants.CODE_REQUEST_ERROR;
		String message = "";
		JSONArray jsonArray = new JSONArray();
		JSONObject seObj;
		String sessionNo = "";//第一期期号
		if (!ParamUtils.chkString(ucode) || !ParamUtils.chkString(betBallArray)
				|| !ParamUtils.chkString(sessionArray)|| !ParamUtils.chkString(betType)) {
			message = APIConstants.PARAMS_EMPTY_MSG;
		} else {// 判断是否在投注时间内
			jsonArray = new JSONArray(sessionArray);
			seObj = jsonArray.getJSONObject(0);
			sessionNo =seObj.get("s").toString(); //追号第一期或者合买期号			

			if(ParamUtils.chkString(sessionNo)){
				GfFiveGaSession gaSession = gfFiveService.getPreviousSessionBySessionNo(sessionNo);
				if(gaSession != null){
					GfFiveGaSession tempSession = (GfFiveGaSession) gfFiveService.getObject(GfFiveGaSession.class, gaSession.getSessionId()-1);
					Date nowDate = DateTimeUtil.getNowSQLDate();
					Date startTime = tempSession.getEndTime();
					Date endTime = gaSession.getEndTime();
					Date fengPanTime = DateTimeUtil.dateDiffSec(endTime, -(int)FiveConstants.FC_BET_END_INTERVAL);// 封盘时间
					long sec1 = DateTimeUtil.dateDiffToSec(startTime, nowDate);
					long sec2 = DateTimeUtil.dateDiffToSec(nowDate, fengPanTime);
					long sec3 = DateTimeUtil.dateDiffToSec(nowDate, endTime);
					if (sec1 < 0) {// 还没到投注时间
						message = "本期还未开盘，请稍后";
					} else if (sec3 < 0) {// 过了开奖时间
						message = "本期已结束，请选择其他投注";
					} else if (sec3 > 0 && sec2 < 0) {// 正好在封盘时间内
						message = "本期已封盘，请等待下期投注";
					}
					GaSessionInfo gaSessionInfo = (GaSessionInfo) CacheUtil.getGameMap().get(Constants.GAME_TYPE_GF_FC);
					if(Constants.PUB_STATUS_CLOSE.equals(gaSessionInfo.getStatus()) || 
							Constants.PUB_STATUS_CLOSE.equals(gaSessionInfo.getBetAvoid())){
						Param tip = gaService.getParam(Constants.PARAM_LOTTERY_CLOSE_TIP);
						if(tip!=null&&ParamUtils.chkString(tip.getValue())){
							message = tip.getValue();
						}else{
							message = "彩种维护";
						}
					}
				}else{
					message = "参数错误！";
				}
			}else{
				message = "参数错误！";
			}
		}

		if (message.equals("")) {
			GfFiveGaSession session = gfFiveService.getCurrentSession();
			if (!sessionNo.equals(session.getSessionNo())) {
				message = "参数错误！";
			}else{
				User user = null;
				Map<String, String> decryptMap = DesUtils.decryptMap(ucode);
				Integer uid = Integer.valueOf(decryptMap
						.get(Constants.DES_KEY_UID));
				user = userService.getValidUser(uid);
				if (user == null) {
				    code = APIConstants.CODE_REQUEST_ERROR;
				    message = "用户不存在";
			    } else {
				    String status = user.getStatus();
				    if(Constants.PUB_STATUS_CLOSE.equals(status)){
					    code = APIConstants.CODE_REQUEST_ERROR;
					    message = "用户无效";
					    map.put("code", code);
					    map.put("msg", message);
					    JsonUtil.AjaxWriter(response, map);
					    return;
				    }
				    if(!ParamUtils.chkString(betType)||(!Constants.PROCUREMENT_SERVICE.equals(betType)&&!Constants.SPONSOR.equals(betType))){
					    code = APIConstants.CODE_REQUEST_ERROR;
					    message = "参数错误";
					    map.put("code", code);
					    map.put("msg", message);
					    JsonUtil.AjaxWriter(response, map);
					    return;
				    }else{
				    	//检查投注有效性
						JSONArray betBallJson = new JSONArray(betBallArray);
						List<String> list = new ArrayList<String>(); // 玩法 +投注号码
						Map<String,Integer> sessionNoMap = new HashMap<String, Integer>(); //期号 倍数
						int betTotalNum;//总投注注数
						int totalMultiple = 0;//投注总倍数
						BigDecimal userMoney = ProductUtil.checkBigDecimal(user.getMoney()); //用户余额
						BigDecimal betMoney = null; //投注金额

						boolean isVerification=false;//验证通过与否
						for(int i=0;i<betBallJson.length();i++){
							JSONObject betBallObj = betBallJson.getJSONObject(i);
							String title = betBallObj.get("t").toString();//玩法
							String betBall = betBallObj.get("b").toString(); //投注球
							if(CqSscConstants.PLAY_TYPE_ZERO.equals(title)){ //玩法为大小单双
								isVerification=CqSscUtil.chkZERO(betBall);
							}else if(CqSscConstants.PLAY_TYPE_ONE.equals(title)){
								isVerification=CqSscUtil.chkONE(betBall);
							}else if(CqSscConstants.PLAY_TYPE_TWO.equals(title)){
								isVerification=CqSscUtil.chkTWO(betBall);
							}else if(CqSscConstants.PLAY_TYPE_THREE.equals(title)){
								isVerification=CqSscUtil.chkTHREE(betBall);
							}else if(CqSscConstants.PLAY_TYPE_FOUR.equals(title)){
								isVerification=CqSscUtil.chkFOUR(betBall);
							}else if(CqSscConstants.PLAY_TYPE_FIVE.equals(title)){
								isVerification=CqSscUtil.chkFIVE(betBall);
							}else if(CqSscConstants.PLAY_TYPE_SIX.equals(title)){
								isVerification=CqSscUtil.chkSIX(betBall);
							}else if(CqSscConstants.PLAY_TYPE_SEVEN.equals(title)){
								isVerification=CqSscUtil.chkSEVEN(betBall);
							}else if(CqSscConstants.PLAY_TYPE_EIGHT.equals(title)){
								isVerification=CqSscUtil.chkEIGHT(betBall);
							}
							if(!isVerification){
								code = APIConstants.CODE_REQUEST_ERROR;
								message = "投注错误！";
								map.put("code", code);
								map.put("msg", message);
								JsonUtil.AjaxWriter(response, map);
								return;
							}
							list.add(title+"+"+betBall);
						}
						//检查投注注数是否大于零。
					    betTotalNum = FiveUtil.getTotalBetNum(list);
						if(betTotalNum<=0){
							message = "您没有投注！";
							code = APIConstants.CODE_REQUEST_ERROR;
							map.put("code", code);
							map.put("msg", message);
							JsonUtil.AjaxWriter(response, map);
							return;
						}

						//检查期号倍数有效性
						for(int i=0;i<jsonArray.length();i++){
						    JSONObject sessionObj = jsonArray.getJSONObject(i);
						    String nextSeNo =sessionObj.get("s").toString();
						    int multiple = Integer.parseInt(sessionObj.get("m").toString());
						    if(Integer.parseInt(nextSeNo)<Integer.parseInt(sessionNo)||multiple==0){
							    message = "期号或者倍数不对";
							    code = APIConstants.CODE_REQUEST_ERROR;
							    map.put("code", code);
							    map.put("msg", message);
							    JsonUtil.AjaxWriter(response, map);
							    return;
						    }
						    sessionNoMap.put(nextSeNo, multiple);
						    totalMultiple = totalMultiple + multiple;
						}

				    	if(Constants.PROCUREMENT_SERVICE.equals(betType)){ //代购
				    		if(!ParamUtils.chkString(isAddNo)||(!FiveConstants.NOT_ADD_NO.equals(isAddNo)&&!FiveConstants.ADD_NO.equals(isAddNo))){
							    code = APIConstants.CODE_REQUEST_ERROR;
							    message = "参数错误";
							    map.put("code", code);
							    map.put("msg", message);
							    JsonUtil.AjaxWriter(response, map);
							    return;
				    		}else{
				    			if(FiveConstants.ADD_NO.equals(isAddNo)){//如果追号
				    				if(!ParamUtils.chkString(isWinStop)||(!FiveConstants.WIN_STOP.equals(isWinStop)&&!FiveConstants.WIN_NO_STOP.equals(isWinStop))){
									    code = APIConstants.CODE_REQUEST_ERROR;
									    message = "参数错误";
									    map.put("code", code);
									    map.put("msg", message);
									    JsonUtil.AjaxWriter(response, map);
									    return;
				    				}
				    			}else{//不追号
									if(jsonArray.length()>1){ //却投注多期
										message = "没有追号不能投注多期！";
										code = APIConstants.CODE_REQUEST_ERROR;
										map.put("code", code);
										map.put("msg", message);
										JsonUtil.AjaxWriter(response, map);
										return;							
									}
				    			}
				    		}
							betMoney = new BigDecimal(betTotalNum*1*totalMultiple);
							// -1 小于 0等于 1大于
							if(betMoney.compareTo(userMoney) == 1){
								message = "您的余额不足！";
								code = APIConstants.CODE_REQUEST_ERROR;
								map.put("code", code);
								map.put("msg", message);
								JsonUtil.AjaxWriter(response, map);
								return;
							}

							user = gfFiveService.saveProcurementServiceBet(user,list,sessionNoMap,isAddNo,isWinStop,betTotalNum);
				    	}else{//合买
				    		if(!ParamUtils.chkString(num)||!ParamUtils.chkString(buyNum)||!ParamUtils.chkString(isGuaranteed)){
								message = "参数错误！";
								code = APIConstants.CODE_REQUEST_ERROR;
								map.put("code", code);
								map.put("msg", message);
								JsonUtil.AjaxWriter(response, map);
								return;
				    		}else{
								int num2 = Integer.parseInt(num); //总份数
								int buyNum2 = Integer.parseInt(buyNum); //购买份数
								int guNum = 0;//默认的保底份数
								if(num2==0 || buyNum2== 0||buyNum2>num2){
									message = "总份数或者购买份数必须大于0！";
									code = APIConstants.CODE_REQUEST_ERROR;
									map.put("code", code);
									map.put("msg", message);
									JsonUtil.AjaxWriter(response, map);
									return;
								}

								if(Constants.GUARANTEE.equals(isGuaranteed)){ //保底
									if(!ParamUtils.chkString(guaranteedNum)){
										message = "保底份额不能为空！";
										code = APIConstants.CODE_REQUEST_ERROR;
										map.put("code", code);
										map.put("msg", message);
										JsonUtil.AjaxWriter(response, map);
										return;
									}else{
										guNum= Integer.parseInt(guaranteedNum);//保底份数

										BigDecimal guarantPrecent = new BigDecimal(guNum).divide(new BigDecimal(num),2, BigDecimal.ROUND_HALF_EVEN);
										if(buyNum2+guNum>num2||guarantPrecent.compareTo(new BigDecimal(Constants.GUARANTEE_PERCENTAGE))<0){ //保底+购买>总份数||保底少于20%
											message = "保底份额必须大于20%！";
											code = APIConstants.CODE_REQUEST_ERROR;
											map.put("code", code);
											map.put("msg", message);
											JsonUtil.AjaxWriter(response, map);
											return;
										}
										betMoney = new BigDecimal(betTotalNum*2*totalMultiple*(buyNum2+guNum)).divide(new BigDecimal(num),2, BigDecimal.ROUND_HALF_EVEN); 
									}
								}else{//不保底
									betMoney = new BigDecimal(betTotalNum*2*totalMultiple*buyNum2).divide(new BigDecimal(num),2, BigDecimal.ROUND_HALF_EVEN); 
								}
								// -1 小于 0等于 1大于
								if(betMoney.compareTo(userMoney) == 1){
									message = "您的余额不足！";
									code = APIConstants.CODE_REQUEST_ERROR;
									map.put("code", code);
									map.put("msg", message);
									JsonUtil.AjaxWriter(response, map);
									return;
								}
								if(new BigDecimal(betTotalNum*2*totalMultiple).divide(new BigDecimal(num2),2, BigDecimal.ROUND_HALF_EVEN).compareTo(new BigDecimal(1))<0){//每份金额不能少于1元
									message = "每份不能少于1积分！";
									code = APIConstants.CODE_REQUEST_ERROR;
									map.put("code", code);
									map.put("msg", message);
									JsonUtil.AjaxWriter(response, map);
									return;
								}
								if(jsonArray.length()>1){ //合买却投注多期
									message = "合买不能投注多期！";
									code = APIConstants.CODE_REQUEST_ERROR;
									map.put("code", code);
									map.put("msg", message);
									JsonUtil.AjaxWriter(response, map);
									return;							
								}

							    user = gfFiveService.saveSponsorBet(session,totalMultiple,user,list,num2,buyNum2,isGuaranteed,guNum,betTotalNum,betMoney);
				    		}
				    	}
						data.put("money", ProductUtil.BigFormatJud(user.getMoney()));
						data.put("point", ProductUtil.BigFormatJud(user.getUserpoints()));
						code = APIConstants.CODE_REQUEST_SUCCESS;
						message = "投注成功！";
				    }
			    }
			}
		}else{
			code = APIConstants.CODE_REQUEST_ERROR;
//			message = "投注无效";
		}
				
		map.put("code", code);
		map.put("msg", message);
		map.put("data", data);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 历史开奖记录
	 * 
	 * @param mapping
	 */
	public void openList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String code = APIConstants.CODE_REQUEST_ERROR;
		String msg = "";

		int pageIndex = ParamUtils.getIntParameter(deParameter, "pageIndex", 0);// 页码
		int pageSize = ParamUtils.getIntParameter(deParameter, "pageSize", 20);// 每页记录数量
		List<Object> para = new ArrayList<Object>();
		StringBuffer hqls = new StringBuffer();
		hqls.append(" and ho.openStatus=? ");// 已开奖
		para.add(Constants.OPEN_STATUS_OPENED);
		hqls.append(" order by  ho.sessionId desc ");// 已开奖
		int statIndex = pageIndex * pageSize;// 计算开始的条数
		int pageNum = 0;
		PaginationSupport ps = gfFiveService.findFcGaSessionList(hqls.toString(),
				para, statIndex, pageSize);
		List<GfFiveGaSession> list = ps.getItems();
		if (list != null & list.size() > 0) {
			pageNum = list.size();
			JSONArray items = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				GfFiveGaSession session = list.get(i);
				JSONObject it = new JSONObject();
				it.put("sessionNo", session.getSessionNo());
				String array[] = session.getOpenResult().split(",|，|\\+|\\|");
				JSONArray jsonArray = new JSONArray();
				for (int j = 0; j < array.length; j++) {
					jsonArray.put(array[j]);
				}
				it.put("resultItems", jsonArray);
				it.put("openResult", ParamUtils.chkStringNotNull(session.getOpenResult()));
				it.put("openTime",
						DateTimeUtil.DateToStringHHMM(session.getOpenTime()));
				it.put("afThree",ThreeUtil.getAfterThree(ParamUtils.chkStringNotNull(session.getOpenResult()))); //后三

				items.put(it);
			}

			data.put("total", ps.getTotalCount());
			data.put("pageIndex", pageIndex);
			data.put("pageSize", pageSize);// 每页条数
			data.put("pageNum", pageNum);// 当前页数量
			data.put("items", items);

			code = APIConstants.CODE_REQUEST_SUCCESS;
			msg = APIConstants.TIPS_OPERATION_SUCC;
			map.put("data", data);
		} else {
			code = APIConstants.CODE_NOT_FOUND;
			msg = APIConstants.TIPS_DATA_NOT;
		}
		map.put("code", code);
		map.put("msg", msg);
		JsonUtil.AjaxWriter(response, map);
	}

	/**
	 * 最新开奖
	 * 
	 * @param mappinglatestLottery
	 */
	public void latestLottery(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String code = APIConstants.CODE_REQUEST_ERROR;
		String msg = "";

		List<Object> para = new ArrayList<Object>();
		StringBuffer hqls = new StringBuffer();
		hqls.append(" and ho.openStatus=? ");// 已开奖
		para.add(Constants.OPEN_STATUS_OPENED);
		hqls.append(" order by  ho.sessionId desc ");// 已开奖
		PaginationSupport ps = gfFiveService.findFcGaSessionList(hqls.toString(),
				para, 0, 10);
		List<GfFiveGaSession> list = ps.getItems();
		if (list != null & list.size() > 0) {
			JSONArray items = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				GfFiveGaSession session = list.get(i);
				JSONObject it = new JSONObject();
				it.put("sessionNo", session.getSessionNo());
				
				String array[] = session.getOpenResult().split(",|，|\\+|\\|");
				JSONArray jsonArray = new JSONArray();
				for (int j = 0; j < array.length; j++) {
					jsonArray.put(array[j]);
				}
				it.put("resultItems", jsonArray);
				it.put("tenths",FiveUtil.getOddAndEven(array[3]));//十分位
				it.put("unit",FiveUtil.getOddAndEven(array[4])); //个位
				it.put("afThree",FiveUtil.getAfterThree(session.getOpenResult())); //后三
				items.put(it);
			}
			data.put("items", items);

			code = APIConstants.CODE_REQUEST_SUCCESS;
			msg = APIConstants.TIPS_OPERATION_SUCC;
			map.put("data", data);
		} else {
			code = APIConstants.CODE_NOT_FOUND;
			msg = APIConstants.TIPS_DATA_NOT;
		}
		map.put("code", code);
		map.put("msg", msg);
		JsonUtil.AjaxWriter(response, map);
	}

	/**
	 * 中奖排行榜
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void winRanking(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String message = "";
		String code = APIConstants.CODE_REQUEST_ERROR;
		List<WinCoDTO> list = null;
		if (message.equals("")) {
			list = gaService.findGaWinCountList(Constants.GAME_TYPE_GF_FC);
			if (list != null && list.size() > 0) {
				JSONArray items = new JSONArray();// 数据集合
				JSONObject obj = null;
				for (WinCoDTO co : list) {
					obj = new JSONObject();
					String name = co.getUser().getUserName();
					if(name.length()<2){
						name = name +"***";
					}else{
						name = name.substring(0,2) + "***";
					}
					obj.put("userName", name); //用户名
					obj.put("totalMoney", co.getGaWinCount().getTotalMoney()); //金额
					items.put(obj);
					data.put("items", items);
				}
				code = APIConstants.CODE_REQUEST_SUCCESS;
				message = "操作成功";
			} else {
				code = APIConstants.CODE_NOT_FOUND;
				message = APIConstants.TIPS_DATA_NOT;
			}
		}
		map.put("data", data);
		map.put("msg", message);
		map.put("code", code);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 走势列表
	 * 
	 * @param
	 */
	public void trend(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);
		String type = ParamUtils.getParameter(deParameter, "type", "4");// 0=个 1=十 2=百 3=千 4=万

		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String code = APIConstants.CODE_REQUEST_ERROR;
		String msg = "";
		List<Object> para = new ArrayList<Object>();
		StringBuffer hqls = new StringBuffer();
		List<GfFiveGaOmit> list=new ArrayList<GfFiveGaOmit>();
		hqls.append(" order by ho.oid desc ");
		list=gfFiveService.findGfFiveGaOmitList(hqls.toString(),para,18);

		JSONArray jsonArray = new JSONArray();
		
		if (list != null && list.size() > 0) {
			
			for(GfFiveGaOmit omit:list){
				JSONObject obj = new JSONObject();
				obj.put("sessionNo", omit.getSessionNo());
				JSONArray subitems = new JSONArray();
				for(int i=0;i<=9;i++){
					Field f = null;
					if("4".equals(type)){
						f=GfFiveGaOmit.class.getDeclaredField("wan"+i);
					}else if("3".equals(type)){
						f=GfFiveGaOmit.class.getDeclaredField("qian"+i);
					}else if("2".equals(type)){
						f=GfFiveGaOmit.class.getDeclaredField("bai"+i);
					}else if("1".equals(type)){
						f=GfFiveGaOmit.class.getDeclaredField("shi"+i);
					}else if("0".equals(type)){
						f=GfFiveGaOmit.class.getDeclaredField("ge"+i);
					}
					f.setAccessible(true);
					if(f.get(omit).toString().equals("0")){
						subitems.put(i+"&");
					}else{
						subitems.put(f.get(omit).toString());
					}
				}
				obj.put("subitems", subitems);
				jsonArray.put(obj);
			}
			data.put("items", jsonArray);
			
			msg = APIConstants.TIPS_OPERATION_SUCC;
			code = APIConstants.CODE_REQUEST_SUCCESS;
		} else {
			msg = APIConstants.TIPS_DATA_NOT;
			code = APIConstants.CODE_NOT_FOUND;
		}
		map.put("data", data);
		map.put("code", code);
		map.put("msg", msg);
		JsonUtil.AjaxWriter(response, map);
	}
	/**
	 * 走势列表
	 * 
	 * @param
	 */
	public void trendWeb(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);
		String type = ParamUtils.getParameter(deParameter, "type", "0");// 0=30期 1=50期 2=100期   3=从多少期到多少期
		String startSessionNo= ParamUtils.getParameter(deParameter, "startSessionNo", "");//开始期号
		String endSessionNo= ParamUtils.getParameter(deParameter, "endSessionNo", "");//开始期号
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String code = APIConstants.CODE_REQUEST_ERROR;
		String msg = "";
		
		List<Object> para = new ArrayList<Object>();
		StringBuffer hqls = new StringBuffer();
		List<GfFiveGaOmit> list=new ArrayList<GfFiveGaOmit>();
		int num=30;

		if("0".equals(type)){
			num=30;
		}else if("1".equals(type)){
			num=50;
		}else if("2".equals(type)){
			num=100;
		}else if("3".equals(type)){
			num=1000;//最大查询1000条
			if(!ParamUtils.chkString(startSessionNo)||!ParamUtils.chkString(endSessionNo)){
				msg="期数填写有误，请重新填写";
			}else{
				if(Integer.parseInt(startSessionNo)>Integer.parseInt(endSessionNo)){
					msg="开始期号不能大于结束期号";
				}
			}
			hqls.append(" and ho.sessionNo>=? ");
			para.add(startSessionNo);
			hqls.append(" and ho.sessionNo<=? ");
			para.add(endSessionNo);
		}else{
			msg ="参数错误";
		}
		if("".equals(msg)){
			hqls.append(" order by  ho.sessionNo desc");
			list=gfFiveService.findGfFiveGaOmitList(hqls.toString(),para,num);

			JSONArray jsonArray = new JSONArray();
			
			if (list != null && list.size() > 0) {
				
				for(GfFiveGaOmit omit:list){
					JSONObject obj = new JSONObject();
					String openResult = omit.getOpenResult();
					String array[] = openResult.split(",|，|\\+|\\|");
					JSONArray openArray = new JSONArray();
					for (int j = 0; j < array.length; j++) {
						openArray.put(array[j]);
					}
					obj.put("resultItems", openArray);//开奖结果

//					obj.put("openResult", openResult);
					obj.put("sessionNo", omit.getSessionNo());
					JSONArray ge = new JSONArray();
					JSONArray shi = new JSONArray();
					JSONArray bai = new JSONArray();
					JSONArray qian = new JSONArray();
					JSONArray wan = new JSONArray();
					JSONArray zu = new JSONArray();
					for(int i=0;i<=9;i++){
						Field f = null;
						f=GfFiveGaOmit.class.getDeclaredField("wan"+i);
						f.setAccessible(true);
						if("0".equals(f.get(omit).toString())){
							wan.put(i+"&");
						}else{
							wan.put(f.get(omit).toString());
						}
						f=GfFiveGaOmit.class.getDeclaredField("qian"+i);
						f.setAccessible(true);
						if("0".equals(f.get(omit).toString())){
							qian.put(i+"&");
						}else{
							qian.put(f.get(omit).toString());
						}

						f=GfFiveGaOmit.class.getDeclaredField("bai"+i);
						f.setAccessible(true);
						if("0".equals(f.get(omit).toString())){
							bai.put(i+"&");
						}else{
							bai.put(f.get(omit).toString());
						}

						f=GfFiveGaOmit.class.getDeclaredField("shi"+i);
						f.setAccessible(true);
						if("0".equals(f.get(omit).toString())){
							shi.put(i+"&");
						}else{
							shi.put(f.get(omit).toString());
						}

						f=GfFiveGaOmit.class.getDeclaredField("ge"+i);
						f.setAccessible(true);
						if("0".equals(f.get(omit).toString())){
							ge.put(i+"&");
						}else{
							ge.put(f.get(omit).toString());
						}
						
						f=GfFiveGaOmit.class.getDeclaredField("zu"+i);
						f.setAccessible(true);
						int z=0;
						for (int j = 0; j < array.length; j++) {
							if(array[j].equals(String.valueOf(i))){
								z=z+1;
							}
						}

						if("0".equals(f.get(omit).toString())){

							zu.put(i+"&" +z);
						}else{
							zu.put(f.get(omit).toString());
						}

						
					}
					obj.put("ge", ge);
					obj.put("shi", shi);
					obj.put("bai", bai);
					obj.put("qian", qian);
					obj.put("wan", wan);
					obj.put("zu", zu);
					jsonArray.put(obj);
				}
				data.put("items", jsonArray);
				
				msg = APIConstants.TIPS_OPERATION_SUCC;
				code = APIConstants.CODE_REQUEST_SUCCESS;
			} else {
				msg = APIConstants.TIPS_DATA_NOT;
				code = APIConstants.CODE_NOT_FOUND;
			}
		}
		map.put("data", data);
		map.put("code", code);
		map.put("msg", msg);
		JsonUtil.AjaxWriter(response, map);
	}

	/**
	 * 游戏规则
	 */
	public void rules(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
		data.put("link", RamConstants.getWebServer() + "/help/fiveRule.html"
				+ "?t=" + System.currentTimeMillis());
		map.put("msg", "");
		map.put("data", data);
		JsonUtil.AjaxWriter(response, map);
	}

}
