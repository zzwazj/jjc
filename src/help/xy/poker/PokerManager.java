package help.xy.poker;

import help.base.APIConstants;

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
import com.apps.eff.TradeCacheUtil;
import com.apps.model.Param;
import com.apps.util.JsonUtil;
import com.apps.util.ProductUtil;
import com.framework.dao.hibernate.PaginationSupport;
import com.framework.util.DateTimeUtil;
import com.framework.util.DesUtils;
import com.framework.util.HQUtils;
import com.framework.util.ParamUtils;
import com.framework.util.StringUtil;
import com.framework.web.action.BaseDispatchAction;
import com.game.model.GaBetOption;
import com.game.model.GaSessionInfo;
import com.game.model.dto.WinCoDTO;
import com.game.service.IGaService;
import com.ram.RamConstants;
import com.ram.model.User;
import com.xy.poker.PokerConstants;
import com.xy.poker.model.PokerGaSession;
import com.xy.poker.model.PokerGaTrend;
import com.xy.poker.service.IPokerService;
import com.xy.poker.util.PokerUtil;


public class PokerManager extends BaseDispatchAction {
	private final IPokerService pokerService = (IPokerService) getService("pokerService");
	private final IGaService gaService = (IGaService) getService("gaService");
	/**
	 * 上期开奖号字串
	 */
	public void previousNo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		PokerGaSession currentSession=pokerService.getCurrentSession();
		if(null==currentSession){
			map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
			map.put("msg", "未到开奖时间");
			map.put("data", data);
		}else{
			PokerGaSession session=pokerService.getPreviousSessionBySessionNo((Integer.parseInt(currentSession.getSessionNo())-1)+"");
			if(session!=null){
				JSONObject obj = new JSONObject();// 返回数据层
				obj.put("sessionNo", session.getSessionNo()==null?"":session.getSessionNo());
				JSONArray openResult = new JSONArray();
				if(session.getOpenResult()!=null){
					
					Map<String, String> resultMap = null;
					String number1 = resultMap.get("number1");
					String number2 = resultMap.get("number2");
					String number3 = resultMap.get("number3");
					String number = resultMap.get("number");
					String colour = resultMap.get("colour");
					//String string = number1+","+number2+","+number3+","+number+","+colour;
					openResult.put(number1);
					openResult.put(number2);
					openResult.put(number3);
					openResult.put(number);
					openResult.put(colour);
				}else{//10 5 7 6 4 1 2 9 8 3
					openResult.put("5");
					openResult.put("3");
					openResult.put("2");
					openResult.put("10");
					openResult.put("1");
				}
				obj.put("openResult", openResult);
				data.put("obj", obj);
				
				map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
				map.put("msg", "操作成功");
				map.put("data", data);
			}else{
				map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
				map.put("msg", "第一期");
				map.put("data", data);
			}
		}

		JsonUtil.AjaxWriter(response, map);
	}

	
	public void currentTime(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);
		String ucode = ParamUtils.getParameter(deParameter, "u");
		String device = ParamUtils.getParameter(deParameter, "device");// 设备类型 1.IOS2安卓
		String ver = ParamUtils.getParameter(deParameter, "ver");// 当前版本
		
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		PokerGaSession session=pokerService.getCurrentSession();
		if(session!=null){
			JSONObject obj = new JSONObject();// 返回数据层
			if(ParamUtils.chkString(ucode)){
				User user =null;
				Integer uid =0;
				Map<String, String> decryptMap = DesUtils.decryptMap(ucode);
				if(decryptMap!=null){
					uid=Integer.valueOf(decryptMap.get(Constants.DES_KEY_UID));
					user = (User) userService.getObject(User.class, uid);
				}
				BigDecimal money = null;
				if(user!=null){
					BigDecimal balance=user.getUserBalance();
					money = TradeCacheUtil.getUserMoney(uid);
					if(balance==null){
						obj.put("gains", "0.0");
					}else{
						obj.put("gains", user.getUserBalance().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
					}
				}else{
					obj.put("gains", "0.0");
				}

				if(money==null) money = new BigDecimal(0);//判空处理
		
				obj.put("money", money.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			}else{
				obj.put("gains", "0.0");
				obj.put("money", "0");
			}
			long openTime=(session.getEndTime().getTime()-System.currentTimeMillis())/1000; //转成秒			
			long betTime=0;
			if(openTime >= PokerConstants.POKER_TIME_OPENING_FREEZE){
				betTime=openTime - PokerConstants.POKER_TIME_OPENING_FREEZE;
			}
			obj.put("gameName", "快乐扑克3");
			obj.put("room", "");
			obj.put("sessionNo", session.getSessionNo());
			obj.put("openDate", DateTimeUtil.DateToStringHHmmss(session.getEndTime()));
			PokerGaSession beforSession=pokerService.getPreviousSessionBySessionNo((Integer.parseInt(session.getSessionNo())-1)+"");				
			if(beforSession==null){
				beforSession=(PokerGaSession) pokerService.getObject(PokerGaSession.class, session.getSessionId()-1);
			}	
			if(beforSession!=null){
				obj.put("lastSessionNo", beforSession.getSessionNo()==null?"":beforSession.getSessionNo());
				JSONArray openResult = new JSONArray();
				if(beforSession.getCountResult()!=null){
					String number1 = beforSession.getCountResult().split(",")[0];
					String number2 = beforSession.getCountResult().split(",")[1];
					String number3 = beforSession.getCountResult().split(",")[2];
					String number = beforSession.getCountResult().split(",")[3];
					String colour = beforSession.getCountResult().split(",")[4];
					//String string = number1+","+number2+","+number3+","+number+","+colour;
					openResult.put(number1);
					openResult.put(number2);
					openResult.put(number3);
					openResult.put(number);
					openResult.put(colour);
				}else{
					if(beforSession.getOpenResult()!=null){
						String result = PokerUtil.openVer(beforSession.getOpenResult());
						String number1 = result.split(",")[0];
						String number2 = result.split(",")[1];
						String number3 = result.split(",")[2];
						String number = result.split(",")[3];
						String colour = result.split(",")[4];

						openResult.put(number1);
						openResult.put(number2);
						openResult.put(number3);
						openResult.put(number);
						openResult.put(colour);
						//测试开奖
//						k10Service.updateK10SessionOpenResultMethod(gaK10Session.getSessionNo(), gaK10Session.getOpenResult());
					}
				}
				obj.put("openResult", openResult);
			}else{
				map.put("msg", "第一期");
				obj.put("lastSessionNo", "");
				obj.put("openResult", new JSONArray());
			}
			obj.put("openTime", Long.toString(openTime));
			obj.put("openDate", DateTimeUtil.DateToStringHHmmss(session.getEndTime()));
			if(betTime<0L){
				obj.put("betTime", "0");
			}else{
				obj.put("betTime", Long.toString(betTime));
			}
			
			String isWeb="0";
			String webUrl="";
			if(device.equals("2")&&"1.0.0".equals(ver)){
				isWeb="1";
				webUrl=StringUtil.checkAPIHttpUrl("/game/init.jsp?t=&m=");//t游戏类型m加密
			}
			obj.put("isWeb", isWeb);
			obj.put("webUrl", webUrl);
			data.put("obj", obj);
			map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
			map.put("msg", "操作成功！");
			map.put("data", data);
		}else{
			map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
			map.put("msg", "开奖时间未到！");
			map.put("data", data);
		}
		JsonUtil.AjaxWriter(response, map);
	}

	/**
	 * 面板选项
	 * @param mapping
	 */
	
	public void betPanel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);
//		String gameType = ParamUtils.getParameter(deParameter, "gameType", "4");//游戏类型
		String playType = ParamUtils.getParameter(deParameter, "playType", "0");// 玩法类型
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String gameName="pokerLiangmianpan";
		if(playType.equals("0")){
			gameName="pokerLiangmianpan";
		}else if(playType.equals("1")){
			gameName="pokerTema";
		}
		String itemsString = CacheUtil.getLotteryBetting(gameName);
		JSONArray betItems=new JSONArray(itemsString);
		data.put("betItems", betItems);
		map.put("data", data);
		map.put("msg", "操作成功");
		map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 下注
	 * @param mapping
	 */
	
	public void bet(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);
		JSONObject map = new JSONObject();// 最外层
		String sessionNo = ParamUtils.getParameter(deParameter, "sessionNo", "");
		String optionArray = ParamUtils.getParameter(deParameter, "optionArray", "");//[{optionId:12,betPoints:2}]
		String room= ParamUtils.getParameter(deParameter, "room", "A");
		String ucode = ParamUtils.getParameter(deParameter, "u");
		String message = "";
		JSONObject data = new JSONObject();
		if (!ParamUtils.chkString(sessionNo) || !ParamUtils.chkString(optionArray)
				|| !ParamUtils.chkString(ucode)) {
			message = APIConstants.PARAMS_EMPTY_MSG;
			map.put("msg", message);
			map.put("code", APIConstants.CODE_REQUEST_ERROR);
		}else{
			PokerGaSession gaK10Session = pokerService
					.getPreviousSessionBySessionNo(sessionNo);
			PokerGaSession tempSession = (PokerGaSession) pokerService.getObject(PokerGaSession.class, gaK10Session.getSessionId()-1);
			Date nowDate = new Date();
			Date startTime = tempSession.getEndTime();
			Date endTime = gaK10Session.getEndTime();
			Date fengPanTime = DateTimeUtil.dateDiffSec(endTime, -PokerConstants.POKER_TIME_OPENING_FREEZE);// 封盘时间
			long sec1 = DateTimeUtil.dateDiffToSec(startTime, nowDate);
			long sec2 = DateTimeUtil.dateDiffToSec(nowDate, fengPanTime);
			long sec3 = DateTimeUtil.dateDiffToSec(nowDate, endTime);
			if (sec1 < 0) {// 还没到投注时间
				message = "本期还未开盘，请稍后";
			} else if (sec3 < 0) {// 过了开奖时间
				message = "本期已结束，请选择其他投注";
			} else if (sec3 > 0 && sec2 < 0) {// 正好在封盘时间内
				message = "本期已开盘，请等待下期投注";
			}
			GaSessionInfo gaSessionInfo = (GaSessionInfo) CacheUtil.getGameMap().get(Constants.GAME_TYPE_XY_POKER);
			if(Constants.PUB_STATUS_CLOSE.equals(gaSessionInfo.getStatus()) || 
					Constants.PUB_STATUS_CLOSE.equals(gaSessionInfo.getBetAvoid())){
				Param tip = gaService.getParam(Constants.PARAM_LOTTERY_CLOSE_TIP);
				if(tip!=null&&ParamUtils.chkString(tip.getValue())){
					message = tip.getValue();
				}else{
					message = "彩种维护";
				}
			}

			if(!message.equals("")){
				map.put("code", APIConstants.CODE_REQUEST_ERROR);
				map.put("msg", message);
				map.put("data", data);
				JsonUtil.AjaxWriter(response, map);
				return;
			}
		}

		if (message.equals("")) {
			PokerGaSession pokerSession=pokerService.getCurrentSession();
			if(pokerSession !=null && sessionNo.equals(pokerSession.getSessionNo())){
				User user = null;
				if(ParamUtils.chkString(optionArray)){
					Integer uid =0;
					Map<String, String> decryptMap = DesUtils.decryptMap(ucode);
					if(decryptMap!=null){
						uid=Integer.valueOf(decryptMap.get(Constants.DES_KEY_UID));
						user = userService.getValidUser(uid);
					}
					if(user==null){
						map.put("msg", "用户不存在");
						map.put("code", APIConstants.CODE_REQUEST_ERROR);
					}else{
						String status = user.getStatus();
					    if(Constants.PUB_STATUS_CLOSE.equals(status)){
						    message = "用户无效";
						    map.put("code", APIConstants.CODE_REQUEST_ERROR);
						    map.put("msg", message);
						    JsonUtil.AjaxWriter(response, map);
						    return;
					    }
						HQUtils hq = new HQUtils(
								"select sum(ho.betMoney) from GaBetDetail ho where 1=1 ");
						hq.addHsql(" and ho.gameType=?  ");
						hq.addPars(Constants.GAME_TYPE_XY_POKER);
						hq.addHsql(" and ho.sessionId=?  ");
						hq.addPars(pokerSession.getSessionId());
						hq.addHsql(" and ho.userId=?  ");
						hq.addPars(uid);
						List<Object> moneylist = gaService.findObjects(hq);
						Integer betMoney = 0;  //当前期已投注过的总金额。
						if (moneylist != null && moneylist.size() > 0) {
							if (moneylist.get(0) != null) {
								betMoney = (Integer) moneylist.get(0);
							} else {
								betMoney = 0;
							}
						}

						JSONArray jsonArray = new JSONArray(optionArray);
						BigDecimal betAll= new BigDecimal(0);
						StringBuffer ids = new StringBuffer();
						Map<Integer,Integer> betMap=new HashMap<Integer,Integer>();

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObj = jsonArray.getJSONObject(i);
//							String optionId = (String) jsonObj.get("id"); //报错
							String optionId = (String) jsonObj.optString("id");
							
							if(optionId !=null && optionId.length() > 0){
								Integer betPoint = Integer.parseInt(jsonObj.get("p").toString()); // 单注投注金额

								if(betPoint > PokerConstants.SINGLE_NOTE_MAX_BET_POINT){
									map.put("code", APIConstants.CODE_REQUEST_ERROR);
									map.put("msg", "单注不能超过"+PokerConstants.SINGLE_NOTE_MAX_BET_POINT+"！");
									JsonUtil.AjaxWriter(response, map);
									return;								
								}
								if(!ParamUtils.chkInteger(betPoint)){
									message="投注不能为0！";
									map.put("code", APIConstants.CODE_REQUEST_ERROR);
									map.put("msg", message);
									JsonUtil.AjaxWriter(response, map);
									return;
								}

								ids=ids.append(jsonObj.get("id").toString()).append(",");
								betAll=betAll.add(new BigDecimal(jsonObj.get("p").toString()));
								betMap.put(Integer.parseInt(jsonObj.get("id").toString()), Integer.parseInt(jsonObj.get("p").toString()));
							}else{
								map.put("code", APIConstants.CODE_REQUEST_ERROR);
								map.put("msg", "参数不正确！");
								JsonUtil.AjaxWriter(response, map);
								return;
							}
						}

						BigDecimal balance = TradeCacheUtil.getUserMoney(uid);
						if(balance==null) balance = new BigDecimal(0);//判空处理
						
//						if (betAll.intValue() + betMoney > PokerConstants.TOTAL_MAX_BET_POINT) {
//							data.put("money", ProductUtil
//									.BigFormatJud(balance));
//							message = "单期投注总额不能超过50000";
//							map.put("code", APIConstants.CODE_REQUEST_ERROR);
//							map.put("msg", message);
//							map.put("data", data);
//							JsonUtil.AjaxWriter(response, map);
//							return;
//						}else 
						if(betAll.intValue()>balance.intValue()){
							data.put("money", ProductUtil.BigFormatJud(balance));
                            map.put("data", data);
							map.put("code", APIConstants.CODE_REQUEST_ERROR);
							map.put("msg", "您的余额不足！");
							JsonUtil.AjaxWriter(response, map);
							return;
						}else{
							String strIds=ids.toString();
							if(ParamUtils.chkString(strIds)){
								strIds=strIds.substring(0, strIds.length()-1);
							}else{
								data.put("money", ProductUtil.BigFormatJud(balance));
	                            map.put("data", data);
								map.put("code", APIConstants.CODE_REQUEST_ERROR);
								map.put("msg", "请选择投注项");
								JsonUtil.AjaxWriter(response, map);
								return;
							}
							List<GaBetOption> list=gaService.getGaBetOptionByIds(strIds);
							for (GaBetOption option : list) {
								String gameType=option.getGameType();
								if(!Constants.GAME_TYPE_XY_POKER.equals(gameType)){
									map.put("code", APIConstants.CODE_REQUEST_ERROR);
									map.put("msg", "请选择正确投注项");
									JsonUtil.AjaxWriter(response, map);
									return;
								}
							}

							user=pokerService.saveUserBetInfo(room,betMap,list,pokerSession,user,betAll);
						}
						BigDecimal money = TradeCacheUtil.getUserMoney(uid);
						if(money==null) 
							money = new BigDecimal(0);
						data.put("money", ProductUtil.BigFormatJud(money));
						map.put("data", data);
						map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
						map.put("msg", "投注成功！");
					}
				}else{
					map.put("msg", "参数不正确");
					map.put("code", APIConstants.CODE_REQUEST_ERROR);
				}
			}else{
				map.put("msg", "参数不正确");
				map.put("code", APIConstants.CODE_REQUEST_ERROR);
			}
		}
		JsonUtil.AjaxWriter(response, map);
	}

	/**
	 * 冷热排行
	 * @param 
	 */
	public void hotRanking(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
	
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		String code=APIConstants.CODE_REQUEST_ERROR;
		String message="";
		List<PokerGaTrend> list=pokerService.findPokerTrendList();
		JSONArray jsonArray = new JSONArray();
		if(list!=null&&list.size()>0){
			for(PokerGaTrend trend:list){
				JSONObject obj = new JSONObject();// 返回数据层
				obj.put("title", trend.getTrendTitle());
				obj.put("num", trend.getTrendCount());
				jsonArray.put(obj);
			}
			code=APIConstants.CODE_REQUEST_SUCCESS;
		}else{
			code=APIConstants.CODE_NOT_FOUND;
			message=APIConstants.TIPS_DATA_NOT;
		}
		data.put("items", jsonArray);
		map.put("code",code );
		map.put("data", data);
		map.put("msg", message);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 走势列表
	 * @param 
	 */
	public void trend(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, String> deParameter = ParamUtils.deParameter(request);
		String type=ParamUtils.getParameter(deParameter, "type","0");//0=号码  1=混合
		int pageNum = ParamUtils.getIntParameter(deParameter, "pgnm", 0);//页码
		int pageSize = ParamUtils.getIntParameter(deParameter, "pgts", 12);//只查询12条
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		List<Object> para = new ArrayList<Object>();
		StringBuffer hqls = new StringBuffer();
		hqls.append(" and ho.openStatus = ? ");//已开奖
		para.add(PokerConstants.POKER_OPEN_STATUS_OPENED);
		hqls.append(" order by  ho.sessionId desc ");//已开奖
		PaginationSupport ps=pokerService.findPokerGaSessionList(hqls.toString(),para,pageNum,pageSize);
		List<PokerGaSession> list=ps.getItems();
		JSONArray jsonArray = new JSONArray();
		if(list!=null&&list.size()>0){
			if(type.equals("0")){//号码
				JSONArray items= new JSONArray();
				items.put("期号");
				items.put("一");
				items.put("二");
				items.put("三");
				items.put("和");
				jsonArray.put(items);
				for(PokerGaSession session:list){
					items= new JSONArray();
					//items.put(session.getSessionNo().substring(session.getSessionNo().length()-3));
					items.put(session.getSessionNo());
					if(session.getCountResult()!=null){
						String number1 = session.getCountResult().split(",")[0];
						String number2 = session.getCountResult().split(",")[1];
						String number3 = session.getCountResult().split(",")[2];
						String number = session.getCountResult().split(",")[3];
						//String string = number1+","+number2+","+number3+","+number+","+colour;
						items.put(number1);
						items.put(number2);
						items.put(number3);
						items.put(number);
					}else{
						String openVer = PokerUtil.openVer(session.getOpenResult());
						String number1 = openVer.split(",")[0];
						String number2 = openVer.split(",")[1];
						String number3 = openVer.split(",")[2];
						String number = openVer.split(",")[3];
						items.put(number1);
						items.put(number2);
						items.put(number3);
						items.put(number);
					}
					jsonArray.put(items);
				}
			}else if(type.equals("1")){//混合
				JSONArray items= new JSONArray();
				items.put("期号");
				items.put("大小");
				items.put("单双");
				items.put("花色");
				items.put("豹子");
				jsonArray.put(items);
				for(PokerGaSession session:list){
					items= new JSONArray();
					//items.put(session.getSessionNo().substring(session.getSessionNo().length()-3));
					items.put(session.getSessionNo());					
					String results=session.getOpenResult();
					Map<String, String> resultMap=null;
					if(session.getCountResult()!=null){
						resultMap = pokerService.judgeCountResult(session.getCountResult());
					}else{
						String openVer = PokerUtil.openVer(results);
						resultMap = pokerService.judgeCountResult(openVer);
					}	
					String string1 = resultMap.get("大小");
					String string2 = resultMap.get("单双");
					String string3 = resultMap.get("花色");
					String string4= resultMap.get("豹子");
					items.put(string1);
					items.put(string2);
					if(ParamUtils.chkString(string3))
						items.put(string3);
					else
						items.put("-");
					if("豹子".equals(string4)){
						String number = resultMap.get("number");
						items.put(string4+number);
					}
					else
						items.put("-");
					jsonArray.put(items);
				}
			}
			data.put("items", jsonArray);
			map.put("msg", "操作成功");
			map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
		}else{
			map.put("msg", "暂无数据");
			map.put("code", APIConstants.CODE_NOT_FOUND);
		}
		map.put("data", data);
		JsonUtil.AjaxWriter(response, map);
	}
	
	
	
	public void rules(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
		data.put("link", RamConstants.getWebServer() + "/help/poker.html"+"?t="+System.currentTimeMillis());
		map.put("msg", "");
		map.put("data", data);
		JsonUtil.AjaxWriter(response, map);
	}
	
	/**
	 * 历史开奖记录
	 * @param mapping
	 */
	public void openList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JSONObject map = new JSONObject();// 最外层
		JSONObject data = new JSONObject();// 返回数据层
		Map<String, String> deParameter = ParamUtils.deParameter(request);		
		int pageIndex = ParamUtils.getIntParameter(deParameter, "pageIndex", 0);//页码
		int pageSize = ParamUtils.getIntParameter(deParameter, "pageSize", 10);//每页记录数量
		List<Object> para = new ArrayList<Object>();
		StringBuffer hqls = new StringBuffer();
		hqls.append(" and ho.openStatus = ? ");//已开奖
		para.add(PokerConstants.POKER_OPEN_STATUS_OPENED);
		hqls.append(" order by  ho.sessionId desc ");//已开奖
		int statIndex = pageIndex * pageSize;// 计算开始的条数
		int pageNum = 0;
		PaginationSupport ps=pokerService.findPokerGaSessionList(hqls.toString(),para,statIndex,pageSize);
		List<PokerGaSession> list=ps.getItems();
		if(list!=null&list.size()>0){
			pageNum=list.size();
			JSONObject obj = new JSONObject();
			JSONArray items = new JSONArray();
			for(int i=0;i<list.size();i++){
				PokerGaSession session=list.get(i);
				JSONObject it = new JSONObject();
				it.put("gameName", "快乐扑克3");
				it.put("openTime", DateTimeUtil.DateToStringHHMM(session.getOpenTime()));
				it.put("sessionNo", session.getSessionNo());
				JSONArray jsonArray=new JSONArray();
				Map<String, String> resultMap = null;
				if(session.getCountResult()!=null){
					 resultMap = pokerService.openCountResult(session.getCountResult());
				}else{
					String openVer = PokerUtil.openVer(session.getOpenResult());
					resultMap = pokerService.judgeCountResult(openVer);
//					resultMap = pokerService.openResult(session.getOpenResult());
				}
				String number1 = resultMap.get("number1");
				String number2 = resultMap.get("number2");
				String number3 = resultMap.get("number3");
				String number = resultMap.get("number");
				String colour = resultMap.get("colour");
				jsonArray.put(number1);
				jsonArray.put(number2);
				jsonArray.put(number3);
				jsonArray.put(number);
				jsonArray.put(colour);
				it.put("resultItems", jsonArray);
				JSONArray sumItems=new JSONArray();
				String string1 = resultMap.get("大小");
				String string2 = resultMap.get("单双");
//				String string3 = resultMap.get("花色");
//				String string4 = resultMap.get("豹子");
				sumItems.put(number);
				sumItems.put(string1+string2);
//				sumItems.put(string3);
//				sumItems.put(string4);
				it.put("sumItems", sumItems);
				String value="";
				if(colour.equals("1")){
					value="对子";
				}else  if(colour.equals("2")){
					value="同花";
				}else  if(colour.equals("3")){
					value="顺子";
				}else  if(colour.equals("4")){
					value="同花顺";
				}else  if(colour.equals("5")){
					value="豹子";
				}else{
					value="散牌";
				}
				it.put("longhu", value);
				items.put(it);
			}
			data.put("items", items);
			data.put("total", ps.getTotalCount());
			data.put("pageIndex", pageIndex);
			data.put("pageSize", pageSize);// 每页条数
			data.put("pageNum", pageNum);// 当前页数量		

			map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
			map.put("msg", "操作成功");
			map.put("data", data);
		}else{
			map.put("code", APIConstants.CODE_REQUEST_ERROR);
			map.put("msg", "没有开奖结果");
		}
//		map.put("code", "");
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
		Map<String, String> deParameter = ParamUtils.deParameter(request);		
		int pageIndex = ParamUtils.getIntParameter(deParameter, "pageIndex", 0);//页码
		int pageSize = ParamUtils.getIntParameter(deParameter, "pageSize", 10);//每页记录数量
		List<Object> para = new ArrayList<Object>();
		StringBuffer hqls = new StringBuffer();
		hqls.append(" and ho.openStatus = ? ");//已开奖
		para.add(PokerConstants.POKER_OPEN_STATUS_OPENED);
		hqls.append(" order by  ho.sessionId desc ");//已开奖
		PaginationSupport ps=pokerService.findPokerGaSessionList(hqls.toString(),para,0,10);
		List<PokerGaSession> list=ps.getItems();
		if(list!=null&list.size()>0){
			JSONObject obj = new JSONObject();
			JSONArray items = new JSONArray();
			for(int i=0;i<list.size();i++){
				PokerGaSession session=list.get(i);
				JSONObject it = new JSONObject();
				it.put("gameName", "快乐扑克3");
				it.put("openTime", DateTimeUtil.DateToStringHHMM(session.getOpenTime()));
				it.put("sessionNo", session.getSessionNo());
				JSONArray jsonArray=new JSONArray();
				Map<String, String> resultMap = null;
				if(session.getCountResult()!=null){
					resultMap = pokerService.openCountResult(session.getCountResult());
				}else{
					String openVer = PokerUtil.openVer(session.getOpenResult());
					resultMap = pokerService.judgeCountResult(openVer);
//					resultMap = pokerService.openResult(session.getOpenResult());
				}
				String number1 = resultMap.get("number1");
				String number2 = resultMap.get("number2");
				String number3 = resultMap.get("number3");
				String number = resultMap.get("number");
				String colour = resultMap.get("colour");
				jsonArray.put(number1);
				jsonArray.put(number2);
				jsonArray.put(number3);
				jsonArray.put(number);
				jsonArray.put(colour);
				it.put("resultItems", jsonArray);
				JSONArray sumItems=new JSONArray();
				String string1 = resultMap.get("大小");
				String string2 = resultMap.get("单双");
//				String string3 = resultMap.get("花色");
//				String string4 = resultMap.get("豹子");
				sumItems.put(number);
				sumItems.put(string1+string2);
//				sumItems.put(string3);
//				sumItems.put(string4);
				it.put("sumItems", sumItems);
				String value="";
				if(colour.equals("1")){
					value="对子";
				}else  if(colour.equals("2")){
					value="同花";
				}else  if(colour.equals("3")){
					value="顺子";
				}else  if(colour.equals("4")){
					value="同花顺";
				}else  if(colour.equals("5")){
					value="豹子";
				}else{
					value="散牌";
				}
				it.put("longhu", value);
				items.put(it);
			}
			data.put("items", items);
			
			map.put("code", APIConstants.CODE_REQUEST_SUCCESS);
			map.put("msg", "操作成功");
			map.put("data", data);
		}else{
			map.put("code", APIConstants.CODE_REQUEST_ERROR);
			map.put("msg", "没有开奖结果");
		}
//		map.put("code", "");
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
			list = gaService.findGaWinCountList(Constants.GAME_TYPE_XY_POKER);
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
				message = "操作失败";
			}
		}
		map.put("data", data);
		map.put("msg", message);
		map.put("code", code);
		JsonUtil.AjaxWriter(response, map);
	}
}
