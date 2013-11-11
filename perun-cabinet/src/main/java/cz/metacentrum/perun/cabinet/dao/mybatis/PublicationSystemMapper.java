package cz.metacentrum.perun.cabinet.dao.mybatis;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PublicationSystemMapper {
	
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int countByExample(PublicationSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int deleteByExample(PublicationSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int insert(PublicationSystem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int insertSelective(PublicationSystem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    List<PublicationSystem> selectByExample(PublicationSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    PublicationSystem selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int updateByExampleSelective(@Param("record") PublicationSystem record, @Param("example") PublicationSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int updateByExample(@Param("record") PublicationSystem record, @Param("example") PublicationSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int updateByPrimaryKeySelective(PublicationSystem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PUBLICATION_SYSTEM
     *
     * @mbggenerated Tue Aug 09 16:18:52 CEST 2011
     */
    int updateByPrimaryKey(PublicationSystem record);

    /**
     * Finds publication system based on passed example
     */
	List<PublicationSystem> findPublicationSystemsByFilter(PublicationSystem filter);
	
}