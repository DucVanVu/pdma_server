package org.pepfar.pdma.app.data.types;

public enum ReportType {

    CBS_REPORT,

    VL_VAAC_REPORT,

    VL_PEPFAR_REPORT,

    VL_PEPFAR_REPORT_OLD,

    VL_DATA,

    CD4_DATA,

    SHI_REPORT,

    CV556_REPORT,

    RISKGROUP_REPORT,

    ARV_DRUG_REPORT,

    // Bệnh nhân điều trị lần đầu
    NEWLY_ENROLLED_PATIENT_REPORT,

    // Bệnh nhân quay lại điều trị
    RETURNED_PATIENT_REPORT,

    // Bệnh nhân được chuyển tới
    TRANSEDIN_PATIENT_REPORT,

    // BN đang được quản lý tại thời điểm xxx
    ACTIVE_PATIENT_REPORT,

    // BN mới đăng ký điều trị trong khoảng từ ngày ... tới ngày
    NEW_PATIENT_REPORT,

    // BN tử vong, bỏ điều trị trong khoảng từ ngày ... tới ngày
    DEAD_LTFU_PATIENT_REPORT,

    // BN được chuyển đi trong khoảng thời gian từ ngày ... tới ngày
    TRANSOUT_PATIENT_REPORT,

    PATIENT_REQUIRING_VL,

    VL_LOGBOOK_REPORT,

    // Báo cáo hoạt động Lao/HIV (Báo cáo quý)
    QUARTERLY_TB_TREATMENT,

    // Báo cáo hoạt động Lao/HIV (Báo cáo tháng)
    MONTHLY_TB_TREATMENT,

    // Lịch làm xét nghiệm VL
    VL_SCHEDULE,

    // Bệnh nhân không có thẻ BHYT (gồm cả bệnh nhân chưa có thông tin thẻ BHYT)
    NO_SHI,

    // Bệnh nhân hết hạn thẻ BHYT
    SHI_EXPIRED,

    // Báo cáo danh sách tổng hợp (đang điều trị, mới điều trị, tử vong, bỏ trị,
    // chuyển đi)
    TX_TABULAR_SUMMARY,

    // Báo cáo chương trình của cơ sở điều trị HIV/AIDS
    OPC_REPORT_MONTHLY,

    // Báo cáo MER cho phòng khám ngoại trú
    MER_OPC,

    MMD_LINELIST_DATA,

    // Yêu cầu dữ liệu của Ly (tháng 9/2021)
    LY_REQUEST_SEP_2021,

    // Yêu cầu dữ liệu của Ly (tháng 9/2021) lần 2
    LY_REQUEST_SEP_2021_2;

    public String getFilenamePrefix() {
        String fname = null;

        switch (this) {
            case CBS_REPORT:
                fname = "bcao_gscb_";
                break;

            case SHI_REPORT:
                fname = "bcao_bhyt_";
                break;

            case CV556_REPORT:
                fname = "bcao_bhyt_cv556_";
                break;

            case VL_PEPFAR_REPORT:
                fname = "bcao_tlvr_pepfar_";
                break;

            case VL_PEPFAR_REPORT_OLD:
                fname = "bcao_tlvr_pepfar_formcu_";
                break;

            case VL_VAAC_REPORT:
                fname = "bcao_tlvr_vaac_";
                break;

            case VL_DATA:
                fname = "dulieu_xn_tlvr_";
                break;

            case CD4_DATA:
                fname = "dulieu_xn_cd4_";
                break;

            case RISKGROUP_REPORT:
                fname = "bcao_pnnc_";
                break;

            case ARV_DRUG_REPORT:
                fname = "bcao_thuoc_arv_";
                break;

            case NEWLY_ENROLLED_PATIENT_REPORT:
                fname = "bcao_bn_dieutri_landau_";
                break;

            case RETURNED_PATIENT_REPORT:
                fname = "bcao_bn_dieutrilai_";
                break;

            case TRANSEDIN_PATIENT_REPORT:
                fname = "bcao_bn_chuyenden_";
                break;

            case ACTIVE_PATIENT_REPORT:
                fname = "bcao_bn_dangquanly_";
                break;

            case NEW_PATIENT_REPORT:
                fname = "bcao_bn_moidieutri_";
                break;

            case DEAD_LTFU_PATIENT_REPORT:
                fname = "bcao_bn_botri_tuvong_";
                break;

            case TRANSOUT_PATIENT_REPORT:
                fname = "bcao_bn_chuyendi_";
                break;

            case PATIENT_REQUIRING_VL:
                fname = "bcao_bn_canlam_tlvr_";
                break;

            case VL_LOGBOOK_REPORT:
                fname = "so_xn_tlvr_";
                break;

            case QUARTERLY_TB_TREATMENT:
                fname = "bcao_laohiv_quy_";
                break;

            case MONTHLY_TB_TREATMENT:
                fname = "bcao_laohiv_thang_";
                break;

            case VL_SCHEDULE:
                fname = "bn_canlam_tlvr_";
                break;

            case NO_SHI:
                fname = "bn_khongco_bhyt_";
                break;

            case SHI_EXPIRED:
                fname = "bn_hethan_bhyt_";
                break;

            case TX_TABULAR_SUMMARY:
                fname = "bcao_tonghop_dieutri_";
                break;

            case OPC_REPORT_MONTHLY:
                fname = "baocao_pknt_";
                break;

            case MER_OPC:
                fname = "baocao_mer_dieutri_";
                break;

            case MMD_LINELIST_DATA:
                fname = "dulieu_capthuoc_nhieuthang_";
                break;

            default:
                fname = "data_";
                break;
        }

        return fname;
    }
}
