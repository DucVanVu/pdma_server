package org.pepfar.pdma.app.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang.ArrayUtils;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.RiskInterview;

import com.google.common.collect.Lists;

public class RiskGroupUtils {

    public static final String RISK_PWID = "risk_1";

    public static final String RISK_MSM = "risk_2";

    public static final String RISK_SW = "risk_3";

    public static final String RISK_TG = "risk_4";

    public static final String RISK_PARTNER = "risk_6";

    public static final String RISK_CLIENT = "risk_8";

    public static final String RISK_PWID_PARTNER = "risk_7";

    public static final String RISK_PRISONER = "risk_5";

    private static final String[] RISK_CODES = {
        RISK_PWID,
        RISK_MSM,
        RISK_SW,
        RISK_TG,
        RISK_PARTNER,
        RISK_CLIENT,
        RISK_PWID_PARTNER,
        RISK_PRISONER
    };

    private static final String[] RISK_NAMES = {
        "Tiêm chích ma tuý",
        "Quan hệ đồng giới nam",
        "Mại dâm",
        "Chuyển giới",
        "Bạn tình người nhiễm",
        "Khách mua dâm",
        "Bạn tình người TCMT",
        "Phạm nhân"
    };

    /**
     * Get a prioritized risk code from an interview
     *
     * @param interview
     * @return
     */
    public static String getPrioritizedRiskCode(RiskInterview interview) {
        if (interview == null || !CommonUtils.isTrue(interview.getRiskIdentified())) {
            return null;
        }

        List<Dictionary> risks = Lists.newArrayList(interview.getRisks());
        List<Integer> indices = new ArrayList<>();

        for (Dictionary r : risks) {
            int idx = ArrayUtils.indexOf(RISK_CODES, r.getCode());
            if (idx >= 0) {
                indices.add(idx);
            }
        }

        int size = indices.size();
        if (size > 1) {
            int selIndex = Collections.min(indices);
            return RISK_CODES[selIndex];
        } else if (size == 1) {
            return RISK_CODES[indices.get(0)];
        } else {
            return null; // other group
        }
    }

    /**
     * Get a prioritized risk code from a list of risk codes
     *
     * @param interview
     * @return
     */
    public static String getPrioritizedRiskCode(List<String> codes) {
        if (CommonUtils.isEmpty(codes)) {
            return null;
        }

        List<Integer> indices = new ArrayList<>();

        for (String code : codes) {
            int idx = ArrayUtils.indexOf(RISK_CODES, code);
            if (idx >= 0) {
                indices.add(idx);
            }
        }

        int size = indices.size();
        if (size > 1) {
            int selIndex = Collections.min(indices);
            return RISK_CODES[selIndex];
        } else if (size == 1) {
            return RISK_CODES[indices.get(0)];
        } else {
            return null; // other group
        }
    }

    /**
     * Get a prioritized risk name from an interview
     *
     * @param interview
     * @return
     */
    public static String getPrioritizedRiskName(RiskInterview interview) {
        if (interview == null || !CommonUtils.isTrue(interview.getRiskIdentified())) {
            return null;
        }

        List<Dictionary> risks = Lists.newArrayList(interview.getRisks());
        List<Integer> indices = new ArrayList<>();

        for (Dictionary r : risks) {
            int idx = ArrayUtils.indexOf(RISK_CODES, r.getCode());
            if (idx >= 0) {
                indices.add(idx);
            }
        }

        int size = indices.size();
        if (size > 1) {
            //			int selIndex = Collections.min(indices);
            //			return RISK_NAMES[selIndex];
            Collections.sort(indices);
            StringJoiner combinedNames = new StringJoiner(",");

            for (int i : indices) {
                combinedNames.add(RISK_NAMES[i]);
            }

            return combinedNames.toString();
        } else if (size == 1) {
            return RISK_NAMES[indices.get(0)];
        } else {
            if (!CommonUtils.isEmpty(interview.getOtherRiskGroupText(), true)) {
                return interview.getOtherRiskGroupText();
            } else {
                return null;
            }
        }
    }
}
