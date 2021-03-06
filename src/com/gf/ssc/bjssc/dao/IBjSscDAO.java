package com.gf.ssc.bjssc.dao;

import java.util.List;

import com.framework.dao.IDAO;
import com.framework.dao.hibernate.PaginationSupport;
import com.game.model.dto.WinCoDTO;
import com.gf.ssc.bjssc.model.GfBjSscGaOmit;
import com.gf.ssc.bjssc.model.GfBjSscGaSession;
import com.gf.ssc.bjssc.model.GfBjSscGaTrend;

public interface IBjSscDAO  extends IDAO {

	/**
	 * 根据系统当前时间获取当前场次
	 * @return
	 */
	public GfBjSscGaSession getCurrentSession();

	/**
	 * 根据期号获取场次相关信息
	 * @param sessionNo
	 * @return
	 */
	public GfBjSscGaSession getPreviousSessionBySessionNo(String sessionNo);

	/**
	 * 冷热排行榜列表
	 * @return
	 */
	public List<GfBjSscGaTrend> findGaTrendList();

	/**
	 * 开奖列表
	 * @return
	 */
	public PaginationSupport findGaSessionList(String hsql,List<Object> para,int pageNum, int pageSize);

//	public List<FcGaSession> getCurrentSession(int start, int end);
	
//	/**
//	 * 查询北京3分彩投注列表
//	 * @param hql
//	 * @param para
//	 * @param pageNum
//	 * @param pageSize
//	 * @return
//	 */
//	public PaginationSupport findFcGaBetList(String hql, List<Object> para,
//			int pageNum, int pageSize);

//	/**
//	 * 投注详细信息
//	 * @param hql
//	 * @param para
//	 * @param pageNum
//	 * @param pageSize
//	 * @return
//	 */
//	public PaginationSupport findGaBetDetail(String hql, List<Object> para,
//			int pageNum, int pageSize);

//	/**
//	 * 根据id查询投注详情
//	 * @param hql
//	 * @param para
//	 * @return
//	 */
//	public List<FcDTO> findGaBetDetailById(String hql, List<Object> para);

	/**
	 * 查询中奖排行榜
	 * @return
	 */
	public List<WinCoDTO> findGaWinCountList();
	
	public GfBjSscGaOmit getFcGaOmitBySessionNo(String sessionNo);

	public List<GfBjSscGaOmit> findGfBjSscGaOmitList(String hql, List<Object> para,
			int num);


	public PaginationSupport findGfBjSscGaSessionList(String hql,
			List<Object> para, int startIndex, int pageSize);

	
	/**
	 * 统计每一种彩票的每一期的投注中奖金额。
	 * @param hqls
	 * @param para
	 * @param startIndex
	 * @param pageSize
	 * @return
	 */
	public PaginationSupport findGameBetAndWinList(String hqls,
			List<Object> para, int startIndex, int pageSize);


}
