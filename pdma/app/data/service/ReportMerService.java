package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.*;

import java.util.List;

public interface ReportMerService {
    List<HTSRecentDto> getDataFacilitySNSRTRIRecent(PreventionFilterDto filter);
    List<HTSRecentDto> getDataFacilitySNSRTRILongTerm(PreventionFilterDto filter);
    List<HTSRecentDto> getDataFacilitySNSRITARecent(PreventionFilterDto filter);
    List<HTSRecentDto> getDataFacilitySNSRITALongTerm(PreventionFilterDto filter);

    List<HTSRecentDto> getDataVCTRTRIRecent(PreventionFilterDto filter);
    List<HTSRecentDto> getDataVCTRTRILongTerm(PreventionFilterDto filter);
    List<HTSRecentDto> getDataVCTRITARecent(PreventionFilterDto filter);
    List<HTSRecentDto> getDataVCTRITALongTerm(PreventionFilterDto filter);

    List<HTSRecentDto> getDataFacilityIndexTestingRTRIRecent(PreventionFilterDto filter);
    List<HTSRecentDto> getDataFacilityIndexTestingRTRILongTerm(PreventionFilterDto filter);
    List<HTSRecentDto> getDataFacilityIndexTestingRITARecent(PreventionFilterDto filter);
    List<HTSRecentDto> getDataFacilityIndexTestingRITALongTerm(PreventionFilterDto filter);

    List<HTSTSTDto> getDataHTSTSTModalityVCTByKP(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityVCTByPositive(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityVCTByNegative(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityOtherCommunityByKP(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityOtherCommunityByPositive(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityOtherCommunityByNegative(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityCommunitySNSByKP(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityCommunitySNSByPositive(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityCommunitySNSByNegative(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityFacilitySNSByKP(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityFacilitySNSByPositive(PreventionFilterDto filter);
    List<HTSTSTDto> getDataHTSTSTModalityFacilitySNSByNegative(PreventionFilterDto filter);

    List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingOffered(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingAccepted(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingContactsElicited(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingKnownPositives(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingNewPositives(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingNewNegatives(PreventionFilterDto filter);

    List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingOffered(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingAccepted(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingContactsElicited(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingKnownPositives(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingDocumentedNegatives(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingNewPositives(PreventionFilterDto filter);
    List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingNewNegatives(PreventionFilterDto filter);


    List<ReportMERPEDto> getDataKPPREV(PreventionFilterDto filter);
    List<ReportMERPEDto> getDataKPPREVC8Positives(PreventionFilterDto filter);
    List<ReportMERPEDto> getDataKPPREVC11Yes(PreventionFilterDto filter);
    List<ReportMERPEDto> getDataKPPREVC11No(PreventionFilterDto filter);

    List<ReportMERPEDto> getDataPPPREVTesting(PreventionFilterDto filter);
    List<ReportMERPEDto> getDataPPPREVPriority(PreventionFilterDto filter);
    List<ReportMERPEDto> getDataPPPREVAgeAndSex(PreventionFilterDto filter);


}
