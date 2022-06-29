package org.pepfar.pdma.app.utils;

import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.repository.RiskInterviewRepository;
import org.pepfar.pdma.app.data.types.Gender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MerReportingUtils {

    @Autowired
    private RiskInterviewRepository riskRepos;

    /**
     * Calculate indicators by riskgroup
     *
     * @param theCase
     * @param arr
     */
    public void calculateIndicatorsByRiskgroup(CaseDto theCase, AtomicInteger[] arr) {
        List<String> codes = riskRepos.findRiskCodesInMostRecentInterview(theCase.getId());
        String riskCode = RiskGroupUtils.getPrioritizedRiskCode(codes);

        if (riskCode == null) {
            // other risk
            arr[5].incrementAndGet();
        } else {
            // one of 8 groups
            switch (riskCode) {
                case RiskGroupUtils.RISK_PWID:
                    arr[0].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_MSM:
                    arr[1].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_TG:
                    arr[2].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_SW:
                    arr[3].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_PRISONER:
                    arr[4].incrementAndGet();
                    break;

                default:
                    arr[5].incrementAndGet();
                    break;
            }
        }
    }

    /**
     * Calculate indicators by age range
     *
     * @param theCase
     * @param arr
     */
    public void calculateIndicatorsByAgeRange(LocalDateTime toDate, CaseDto theCase, AtomicInteger[] arr) {
        PersonDto person = theCase.getPerson();
        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, toDate);
        int i = -1;

        if (age < 1) {
            if (gender == Gender.MALE) {
                i = 23;
            } else {
                i = 7; // to avoid missing patients
            }
        } else {
            for (int k = 0; k < 14; k++) {
                int lbound = 1;
                int ubound = 4;

                if (k > 0) {
                    lbound = k * 5;

                    if (k < 10) {
                        ubound = lbound + 4;
                    } else {
                        ubound = Integer.MAX_VALUE;
                    }
                }

                if (age >= lbound && age <= ubound) {
                    if (gender == Gender.MALE) {
                        i = k + 24;
                    } else {
                        i = k + 8; // to avoid missing patients
                    }
                }
            }
        }

        if (i >= 0) {
            arr[i].incrementAndGet();
        }
    }
}
