package com.xy.ssc.cqssc.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.apps.eff.dto.SessionItem;
import com.framework.dao.hibernate.PaginationSupport;
import com.framework.service.IService;
import com.game.model.GaBetOption;
import com.ram.model.User;
import com.xy.ssc.cqssc.model.CqSscGaSession;
import com.xy.ssc.cqssc.model.CqSscGaTrend;
import com.xy.ssc.cqssc.model.dto.CqSscDTO;

public interface ICqSscService   extends IService {
	/**
	 * 初始化明天场次
	 */
	public String updateInitSession();
	/**
	 * 初始化今天场次
	 */
	public String updateInitTodaySession();
	public String updateFetchAndOpenResult(Map<String, SessionItem> sessionNoMap);
	//public void updateFetchAndOpenTrend(Integer count);
	/**
	 * 获取当前场次，根据系统时间从数据库查询
	 * @return
	 */
	public CqSscGaSession getCurrentSession();
	/**
	 * 根据期号获取当前期
	 * @param sessionNo
	 * @return
	 */
	public CqSscGaSession getPreviousSessionBySessionNo(String sessionNo);
	/**
	 * 保存用户投注信息
	 */
	public User saveUserBetInfo(String room,Map<String,Integer> betMap,List<GaBetOption> list,CqSscGaSession gaK10Session,User user,BigDecimal betAll);
	/**
	 * 冷热排行列表
	 * @return
	 */
	public List<CqSscGaTrend> findCqSscGaTrendList();
	/**
	 * 开奖列表
	 */
	public PaginationSupport  findCqSscGaSessionList(String hql, List<Object> para,int pageNum,int pageSize);
	/**
	 * 保存单期开奖结果
	 */
	public boolean saveOpenResult(CqSscGaSession session,String openResult);
	
	/**
	 * 保存单期开奖结果
	 */
	public boolean saveAndOpenResult(CqSscGaSession session,String openResult);
	/**
	 * 统计每期数据
	 * @return
	 */
	public PaginationSupport  findCqSscGaBetList(String hql, List<Object> para,int pageNum,int pageSize);

	/**
	 * 获取投注详细信息
	 * @param hql
	 * @param para
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PaginationSupport findGaBetDetail(String hql,
			List<Object> para, int pageNum, int pageSize);
	
	/**
	 * 根据id查询投注详情。
	 * @param string
	 * @param para
	 * @return
	 */
	public List<CqSscDTO> findGaBetDetailById(String string, List<Object> para);
	
	/**
	 * 派彩错误，撤回已经派彩的金额
	 * @param session
	 * @return
	 */
	public boolean saveRevokePrize(CqSscGaSession session);

}
