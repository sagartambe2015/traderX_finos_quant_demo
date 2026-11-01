package com.quantraksha.kite.persist.entity;


/**Objective: This class is used to store the study, research, backtest and signal ids for a strategy.
 *
 * @author : sagar tambe
 */
public class StudyResearchBackEntity {

    /**
     * Objective: This class is used to store the study, research, backtest and signal ids for a strategy.
     */
    private String strategyId;

    /**
     * Objective: This help to relate study behind this strategy.
     */
    private String studyId;

    /**
     * Objective: This help to relate research behind this strategy.
     */
    private String researchId;


    /**
     * Objective: This help to relate backtest behind this strategy.
     */
    private String backtestId;

    /**
     * Objective: This help to relate signal behind this strategy.
     */
    private String signalId;

}
