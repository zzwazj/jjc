package com.xy.ssc.xjssc.dao;

import java.util.List;

import com.framework.dao.IDAO;
import com.framework.dao.hibernate.PaginationSupport;
import com.xy.ssc.xjssc.model.XjSscGaSession;
import com.xy.ssc.xjssc.model.XjSscGaTrend;
import com.xy.ssc.xjssc.model.dto.XjSscDTO;


public interface IXjSscDAO  extends IDAO {
	/**
	 * 获取当前场次，根据系统时间从数据库查询
	 * @return
	 */
	public XjSscGaSession getCurrentSession();
	/**
	 * 根据期号获取当前期
	 * @param sessionNo
	 * @return
	 */
	public XjSscGaSession getPreviousSessionBySessionNo(String sessionNo);
	/**
	 * 冷热排行列表
	 * @return
	 */
	public List<XjSscGaTrend> findXjSscGaTrendList();
	/**
	 * 冷热排行所有数据
	 * @return
	 */
	public List<XjSscGaTrend> findXjSscGaTrendAllList();
	/**
	 * 开奖列表
	 */
	public PaginationSupport  findXjSscGaSessionList(String hql, List<Object> para,int pageNum,int pageSize);
	/**
	 * 统计每期数据
	 * @return
	 */
	public PaginationSupport  findXjSscGaBetList(String hql, List<Object> para,int pageNum,int pageSize);

	/**
	 * 投注详细信息
	 * @param hql
	 * @param para
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PaginationSupport findGaBetDetail(String hql, List<Object> para,
			int pageNum, int pageSize);
	
	/**
	 * 根据id查询投注详情
	 * @param hql
	 * @param para
	 * @return
	 */
	public List<XjSscDTO> findGaBetDetailById(String hql, List<Object> para);
	/**
	 * 删除XjSscGaBet表数据
	 * @param hql
	 * @param para
	 */
	public void deleteXjSscGaBet(String hql, List<Object> para);
}
