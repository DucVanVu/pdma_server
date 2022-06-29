package org.pepfar.pdma.app.data.dto;

import java.math.BigInteger;

public class HTSRecentDto {
    private BigInteger orgId;
    private String orgCode;
    private String orgName;
    private String provinceName;
    private String districtName;
    private String support;
    private String modality;

    private BigInteger pwidRTRI = new BigInteger("0");
    private BigInteger msmRTRI = new BigInteger("0");
    private BigInteger tgRTRI = new BigInteger("0");
    private BigInteger fswRTRI = new BigInteger("0");
    private BigInteger otherRTRI = new BigInteger("0");

    private BigInteger fuRTRI = new BigInteger("0");
    private BigInteger f15RTRI = new BigInteger("0");
    private BigInteger f20RTRI = new BigInteger("0");
    private BigInteger f25RTRI = new BigInteger("0");
    private BigInteger f30RTRI = new BigInteger("0");
    private BigInteger f35RTRI = new BigInteger("0");
    private BigInteger f40RTRI = new BigInteger("0");
    private BigInteger f45RTRI = new BigInteger("0");
    private BigInteger f50RTRI = new BigInteger("0");

    private BigInteger muRTRI = new BigInteger("0");
    private BigInteger m15RTRI = new BigInteger("0");
    private BigInteger m20RTRI = new BigInteger("0");
    private BigInteger m25RTRI = new BigInteger("0");
    private BigInteger m30RTRI = new BigInteger("0");
    private BigInteger m35RTRI = new BigInteger("0");
    private BigInteger m40RTRI = new BigInteger("0");
    private BigInteger m45RTRI = new BigInteger("0");
    private BigInteger m50RTRI = new BigInteger("0");

    private Integer totalRTRIRecent;

    private BigInteger pwidRTRILongTerm = new BigInteger("0");
    private BigInteger msmRTRILongTerm = new BigInteger("0");
    private BigInteger tgRTRILongTerm = new BigInteger("0");
    private BigInteger fswRTRILongTerm = new BigInteger("0");
    private BigInteger otherRTRILongTerm = new BigInteger("0");

    private BigInteger fuRTRILongTerm = new BigInteger("0");
    private BigInteger f15RTRILongTerm = new BigInteger("0");
    private BigInteger f20RTRILongTerm = new BigInteger("0");
    private BigInteger f25RTRILongTerm = new BigInteger("0");
    private BigInteger f30RTRILongTerm = new BigInteger("0");
    private BigInteger f35RTRILongTerm = new BigInteger("0");
    private BigInteger f40RTRILongTerm = new BigInteger("0");
    private BigInteger f45RTRILongTerm = new BigInteger("0");
    private BigInteger f50RTRILongTerm = new BigInteger("0");

    private BigInteger muRTRILongTerm = new BigInteger("0");
    private BigInteger m15RTRILongTerm = new BigInteger("0");
    private BigInteger m20RTRILongTerm = new BigInteger("0");
    private BigInteger m25RTRILongTerm = new BigInteger("0");
    private BigInteger m30RTRILongTerm = new BigInteger("0");
    private BigInteger m35RTRILongTerm = new BigInteger("0");
    private BigInteger m40RTRILongTerm = new BigInteger("0");
    private BigInteger m45RTRILongTerm = new BigInteger("0");
    private BigInteger m50RTRILongTerm = new BigInteger("0");

    private Integer totalRTRILongTerm;

    private Integer totalRTRI;

    private BigInteger pwidRITA = new BigInteger("0");
    private BigInteger msmRITA = new BigInteger("0");
    private BigInteger tgRITA = new BigInteger("0");
    private BigInteger fswRITA = new BigInteger("0");
    private BigInteger otherRITA = new BigInteger("0");

    private BigInteger fuRITA = new BigInteger("0");
    private BigInteger f15RITA = new BigInteger("0");
    private BigInteger f20RITA = new BigInteger("0");
    private BigInteger f25RITA = new BigInteger("0");
    private BigInteger f30RITA = new BigInteger("0");
    private BigInteger f35RITA = new BigInteger("0");
    private BigInteger f40RITA = new BigInteger("0");
    private BigInteger f45RITA = new BigInteger("0");
    private BigInteger f50RITA = new BigInteger("0");

    private BigInteger muRITA = new BigInteger("0");
    private BigInteger m15RITA = new BigInteger("0");
    private BigInteger m20RITA = new BigInteger("0");
    private BigInteger m25RITA = new BigInteger("0");
    private BigInteger m30RITA = new BigInteger("0");
    private BigInteger m35RITA = new BigInteger("0");
    private BigInteger m40RITA = new BigInteger("0");
    private BigInteger m45RITA = new BigInteger("0");
    private BigInteger m50RITA = new BigInteger("0");

    private Integer totalRITARecent;

    private BigInteger pwidRITALongTerm = new BigInteger("0");
    private BigInteger msmRITALongTerm = new BigInteger("0");
    private BigInteger tgRITALongTerm = new BigInteger("0");
    private BigInteger fswRITALongTerm = new BigInteger("0");
    private BigInteger otherRITALongTerm = new BigInteger("0");

    private BigInteger fuRITALongTerm = new BigInteger("0");
    private BigInteger f15RITALongTerm = new BigInteger("0");
    private BigInteger f20RITALongTerm = new BigInteger("0");
    private BigInteger f25RITALongTerm = new BigInteger("0");
    private BigInteger f30RITALongTerm = new BigInteger("0");
    private BigInteger f35RITALongTerm = new BigInteger("0");
    private BigInteger f40RITALongTerm = new BigInteger("0");
    private BigInteger f45RITALongTerm = new BigInteger("0");
    private BigInteger f50RITALongTerm = new BigInteger("0");

    private BigInteger muRITALongTerm = new BigInteger("0");
    private BigInteger m15RITALongTerm = new BigInteger("0");
    private BigInteger m20RITALongTerm = new BigInteger("0");
    private BigInteger m25RITALongTerm = new BigInteger("0");
    private BigInteger m30RITALongTerm = new BigInteger("0");
    private BigInteger m35RITALongTerm = new BigInteger("0");
    private BigInteger m40RITALongTerm = new BigInteger("0");
    private BigInteger m45RITALongTerm = new BigInteger("0");
    private BigInteger m50RITALongTerm = new BigInteger("0");

    private Integer totalRITALongTerm;

    private Integer totalRITA;

    public BigInteger getOrgId() {
        return orgId;
    }

    public void setOrgId(BigInteger orgId) {
        this.orgId = orgId;
    }

    public String getOrgCode() {return orgCode;}

    public void setOrgCode(String orgCode) {this.orgCode = orgCode;}

    public String getOrgName() {return orgName;}

    public void setOrgName(String orgName) {this.orgName = orgName;}

    public String getProvinceName() {return provinceName;}

    public void setProvinceName(String provinceName) {this.provinceName = provinceName;}

    public String getDistrictName() {return districtName;}

    public void setDistrictName(String districtName) {this.districtName = districtName;}

    public String getSupport() {return support;}

    public void setSupport(String support) {this.support = support;}

    public String getModality() {return modality;}

    public void setModality(String modality) {this.modality = modality;}

    public BigInteger getPwidRTRI() {return pwidRTRI;}

    public void setPwidRTRI(BigInteger pwidRTRI) {this.pwidRTRI = pwidRTRI;}

    public BigInteger getMsmRTRI() {return msmRTRI;}

    public void setMsmRTRI(BigInteger msmRTRI) {this.msmRTRI = msmRTRI;}

    public BigInteger getTgRTRI() {return tgRTRI;}

    public void setTgRTRI(BigInteger tgRTRI) {this.tgRTRI = tgRTRI;}

    public BigInteger getFswRTRI() {return fswRTRI;}

    public void setFswRTRI(BigInteger fswRTRI) {this.fswRTRI = fswRTRI;}

    public BigInteger getOtherRTRI() {return otherRTRI;}

    public void setOtherRTRI(BigInteger otherRTRI) {this.otherRTRI = otherRTRI;}

    public BigInteger getFuRTRI() {return fuRTRI;}

    public void setFuRTRI(BigInteger fuRTRI) {this.fuRTRI = fuRTRI;}

    public BigInteger getF15RTRI() {return f15RTRI;}

    public void setF15RTRI(BigInteger f15RTRI) {this.f15RTRI = f15RTRI;}

    public BigInteger getF20RTRI() {return f20RTRI;}

    public void setF20RTRI(BigInteger f20RTRI) {this.f20RTRI = f20RTRI;}

    public BigInteger getF25RTRI() {return f25RTRI;}

    public void setF25RTRI(BigInteger f25RTRI) {this.f25RTRI = f25RTRI;}

    public BigInteger getF30RTRI() {return f30RTRI;}

    public void setF30RTRI(BigInteger f30RTRI) {this.f30RTRI = f30RTRI;}

    public BigInteger getF35RTRI() {return f35RTRI;}

    public void setF35RTRI(BigInteger f35RTRI) {this.f35RTRI = f35RTRI;}

    public BigInteger getF40RTRI() {return f40RTRI;}

    public void setF40RTRI(BigInteger f40RTRI) {this.f40RTRI = f40RTRI;}

    public BigInteger getF45RTRI() {return f45RTRI;}

    public void setF45RTRI(BigInteger f45RTRI) {this.f45RTRI = f45RTRI;}

    public BigInteger getF50RTRI() {return f50RTRI;}

    public void setF50RTRI(BigInteger f50RTRI) {this.f50RTRI = f50RTRI;}

    public BigInteger getMuRTRI() {return muRTRI;}

    public void setMuRTRI(BigInteger muRTRI) {this.muRTRI = muRTRI;}

    public BigInteger getM15RTRI() {return m15RTRI;}

    public void setM15RTRI(BigInteger m15RTRI) {this.m15RTRI = m15RTRI;}

    public BigInteger getM20RTRI() {return m20RTRI;}

    public void setM20RTRI(BigInteger m20RTRI) {this.m20RTRI = m20RTRI;}

    public BigInteger getM25RTRI() {return m25RTRI;}

    public void setM25RTRI(BigInteger m25RTRI) {this.m25RTRI = m25RTRI;}

    public BigInteger getM30RTRI() {return m30RTRI;}

    public void setM30RTRI(BigInteger m30RTRI) {this.m30RTRI = m30RTRI;}

    public BigInteger getM35RTRI() {return m35RTRI;}

    public void setM35RTRI(BigInteger m35RTRI) {this.m35RTRI = m35RTRI;}

    public BigInteger getM40RTRI() {return m40RTRI;}

    public void setM40RTRI(BigInteger m40RTRI) {this.m40RTRI = m40RTRI;}

    public BigInteger getM45RTRI() {return m45RTRI;}

    public void setM45RTRI(BigInteger m45RTRI) {this.m45RTRI = m45RTRI;}

    public BigInteger getM50RTRI() {return m50RTRI;}

    public void setM50RTRI(BigInteger m50RTRI) {this.m50RTRI = m50RTRI;}

    public Integer getTotalRTRIRecent() {
        totalRTRIRecent = 0;
        if (fuRTRI != null) {
            totalRTRIRecent += fuRTRI.intValue();
        }
        if (f15RTRI != null) {
            totalRTRIRecent += f15RTRI.intValue();
        }
        if (f20RTRI != null) {
            totalRTRIRecent += f20RTRI.intValue();
        }
        if (f25RTRI != null) {
            totalRTRIRecent += f25RTRI.intValue();
        }
        if (f30RTRI != null) {
            totalRTRIRecent += f30RTRI.intValue();
        }
        if (f35RTRI != null) {
            totalRTRIRecent += f35RTRI.intValue();
        }
        if (f40RTRI != null) {
            totalRTRIRecent += f40RTRI.intValue();
        }
        if (f45RTRI != null) {
            totalRTRIRecent += f45RTRI.intValue();
        }
        if (f50RTRI != null) {
            totalRTRIRecent += f50RTRI.intValue();
        }

        if (muRTRI != null) {
            totalRTRIRecent += muRTRI.intValue();
        }
        if (m15RTRI != null) {
            totalRTRIRecent += m15RTRI.intValue();
        }
        if (m20RTRI != null) {
            totalRTRIRecent += m20RTRI.intValue();
        }
        if (m25RTRI != null) {
            totalRTRIRecent += m25RTRI.intValue();
        }
        if (m30RTRI != null) {
            totalRTRIRecent += m30RTRI.intValue();
        }
        if (m35RTRI != null) {
            totalRTRIRecent += m35RTRI.intValue();
        }
        if (m40RTRI != null) {
            totalRTRIRecent += m40RTRI.intValue();
        }
        if (m45RTRI != null) {
            totalRTRIRecent += m45RTRI.intValue();
        }
        if (m50RTRI != null) {
            totalRTRIRecent += m50RTRI.intValue();
        }
        return totalRTRIRecent;
    }

    public void setTotalRTRIRecent(Integer totalRTRIRecent) {this.totalRTRIRecent = totalRTRIRecent;}

    public BigInteger getPwidRTRILongTerm() {return pwidRTRILongTerm;}

    public void setPwidRTRILongTerm(BigInteger pwidRTRILongTerm) {this.pwidRTRILongTerm = pwidRTRILongTerm;}

    public BigInteger getMsmRTRILongTerm() {return msmRTRILongTerm;}

    public void setMsmRTRILongTerm(BigInteger msmRTRILongTerm) {this.msmRTRILongTerm = msmRTRILongTerm;}

    public BigInteger getTgRTRILongTerm() {return tgRTRILongTerm;}

    public void setTgRTRILongTerm(BigInteger tgRTRILongTerm) {this.tgRTRILongTerm = tgRTRILongTerm;}

    public BigInteger getFswRTRILongTerm() {return fswRTRILongTerm;}

    public void setFswRTRILongTerm(BigInteger fswRTRILongTerm) {this.fswRTRILongTerm = fswRTRILongTerm;}

    public BigInteger getOtherRTRILongTerm() {return otherRTRILongTerm;}

    public void setOtherRTRILongTerm(BigInteger otherRTRILongTerm) {this.otherRTRILongTerm = otherRTRILongTerm;}

    public BigInteger getFuRTRILongTerm() {return fuRTRILongTerm;}

    public void setFuRTRILongTerm(BigInteger fuRTRILongTerm) {this.fuRTRILongTerm = fuRTRILongTerm;}

    public BigInteger getF15RTRILongTerm() {return f15RTRILongTerm;}

    public void setF15RTRILongTerm(BigInteger f15RTRILongTerm) {this.f15RTRILongTerm = f15RTRILongTerm;}

    public BigInteger getF20RTRILongTerm() {return f20RTRILongTerm;}

    public void setF20RTRILongTerm(BigInteger f20RTRILongTerm) {this.f20RTRILongTerm = f20RTRILongTerm;}

    public BigInteger getF25RTRILongTerm() {return f25RTRILongTerm;}

    public void setF25RTRILongTerm(BigInteger f25RTRILongTerm) {this.f25RTRILongTerm = f25RTRILongTerm;}

    public BigInteger getF30RTRILongTerm() {return f30RTRILongTerm;}

    public void setF30RTRILongTerm(BigInteger f30RTRILongTerm) {this.f30RTRILongTerm = f30RTRILongTerm;}

    public BigInteger getF35RTRILongTerm() {return f35RTRILongTerm;}

    public void setF35RTRILongTerm(BigInteger f35RTRILongTerm) {this.f35RTRILongTerm = f35RTRILongTerm;}

    public BigInteger getF40RTRILongTerm() {return f40RTRILongTerm;}

    public void setF40RTRILongTerm(BigInteger f40RTRILongTerm) {this.f40RTRILongTerm = f40RTRILongTerm;}

    public BigInteger getF45RTRILongTerm() {return f45RTRILongTerm;}

    public void setF45RTRILongTerm(BigInteger f45RTRILongTerm) {this.f45RTRILongTerm = f45RTRILongTerm;}

    public BigInteger getF50RTRILongTerm() {return f50RTRILongTerm;}

    public void setF50RTRILongTerm(BigInteger f50RTRILongTerm) {this.f50RTRILongTerm = f50RTRILongTerm;}

    public BigInteger getMuRTRILongTerm() {return muRTRILongTerm;}

    public void setMuRTRILongTerm(BigInteger muRTRILongTerm) {this.muRTRILongTerm = muRTRILongTerm;}

    public BigInteger getM15RTRILongTerm() {return m15RTRILongTerm;}

    public void setM15RTRILongTerm(BigInteger m15RTRILongTerm) {this.m15RTRILongTerm = m15RTRILongTerm;}

    public BigInteger getM20RTRILongTerm() {return m20RTRILongTerm;}

    public void setM20RTRILongTerm(BigInteger m20RTRILongTerm) {this.m20RTRILongTerm = m20RTRILongTerm;}

    public BigInteger getM25RTRILongTerm() {return m25RTRILongTerm;}

    public void setM25RTRILongTerm(BigInteger m25RTRILongTerm) {this.m25RTRILongTerm = m25RTRILongTerm;}

    public BigInteger getM30RTRILongTerm() {return m30RTRILongTerm;}

    public void setM30RTRILongTerm(BigInteger m30RTRILongTerm) {this.m30RTRILongTerm = m30RTRILongTerm;}

    public BigInteger getM35RTRILongTerm() {return m35RTRILongTerm;}

    public void setM35RTRILongTerm(BigInteger m35RTRILongTerm) {this.m35RTRILongTerm = m35RTRILongTerm;}

    public BigInteger getM40RTRILongTerm() {return m40RTRILongTerm;}

    public void setM40RTRILongTerm(BigInteger m40RTRILongTerm) {this.m40RTRILongTerm = m40RTRILongTerm;}

    public BigInteger getM45RTRILongTerm() {return m45RTRILongTerm;}

    public void setM45RTRILongTerm(BigInteger m45RTRILongTerm) {this.m45RTRILongTerm = m45RTRILongTerm;}

    public BigInteger getM50RTRILongTerm() {return m50RTRILongTerm;}

    public void setM50RTRILongTerm(BigInteger m50RTRILongTerm) {this.m50RTRILongTerm = m50RTRILongTerm;}

    public Integer getTotalRTRILongTerm() {
        totalRTRILongTerm = 0;
        if (fuRTRILongTerm != null) {
            totalRTRILongTerm += fuRTRILongTerm.intValue();
        }
        if (f15RTRILongTerm != null) {
            totalRTRILongTerm += f15RTRILongTerm.intValue();
        }
        if (f20RTRILongTerm != null) {
            totalRTRILongTerm += f20RTRILongTerm.intValue();
        }
        if (f25RTRILongTerm != null) {
            totalRTRILongTerm += f25RTRILongTerm.intValue();
        }
        if (f30RTRILongTerm != null) {
            totalRTRILongTerm += f30RTRILongTerm.intValue();
        }
        if (f35RTRILongTerm != null) {
            totalRTRILongTerm += f35RTRILongTerm.intValue();
        }
        if (f40RTRILongTerm != null) {
            totalRTRILongTerm += f40RTRILongTerm.intValue();
        }
        if (f45RTRILongTerm != null) {
            totalRTRILongTerm += f45RTRILongTerm.intValue();
        }
        if (f50RTRILongTerm != null) {
            totalRTRILongTerm += f50RTRILongTerm.intValue();
        }

        if (muRTRILongTerm != null) {
            totalRTRILongTerm += muRTRILongTerm.intValue();
        }
        if (m15RTRILongTerm != null) {
            totalRTRILongTerm += m15RTRILongTerm.intValue();
        }
        if (m20RTRILongTerm != null) {
            totalRTRILongTerm += m20RTRILongTerm.intValue();
        }
        if (m25RTRILongTerm != null) {
            totalRTRILongTerm += m25RTRILongTerm.intValue();
        }
        if (m30RTRILongTerm != null) {
            totalRTRILongTerm += m30RTRILongTerm.intValue();
        }
        if (m35RTRILongTerm != null) {
            totalRTRILongTerm += m35RTRILongTerm.intValue();
        }
        if (m40RTRILongTerm != null) {
            totalRTRILongTerm += m40RTRILongTerm.intValue();
        }
        if (m45RTRILongTerm != null) {
            totalRTRILongTerm += m45RTRILongTerm.intValue();
        }
        if (m50RTRILongTerm != null) {
            totalRTRILongTerm += m50RTRILongTerm.intValue();
        }
        return totalRTRILongTerm;
    }

    public void setTotalRTRILongTerm(Integer totalRTRILongTerm) {this.totalRTRILongTerm = totalRTRILongTerm;}

    public Integer getTotalRTRI() {
        totalRTRI = this.getTotalRTRILongTerm() + this.getTotalRTRIRecent();
        return totalRTRI;
    }

    public void setTotalRTRI(Integer totalRTRI) {this.totalRTRI = totalRTRI;}

    public BigInteger getPwidRITA() {return pwidRITA;}

    public void setPwidRITA(BigInteger pwidRITA) {this.pwidRITA = pwidRITA;}

    public BigInteger getMsmRITA() {return msmRITA;}

    public void setMsmRITA(BigInteger msmRITA) {this.msmRITA = msmRITA;}

    public BigInteger getTgRITA() {return tgRITA;}

    public void setTgRITA(BigInteger tgRITA) {this.tgRITA = tgRITA;}

    public BigInteger getFswRITA() {return fswRITA;}

    public void setFswRITA(BigInteger fswRITA) {this.fswRITA = fswRITA;}

    public BigInteger getOtherRITA() {return otherRITA;}

    public void setOtherRITA(BigInteger otherRITA) {this.otherRITA = otherRITA;}

    public BigInteger getFuRITA() {return fuRITA;}

    public void setFuRITA(BigInteger fuRITA) {this.fuRITA = fuRITA;}

    public BigInteger getF15RITA() {return f15RITA;}

    public void setF15RITA(BigInteger f15RITA) {this.f15RITA = f15RITA;}

    public BigInteger getF20RITA() {return f20RITA;}

    public void setF20RITA(BigInteger f20RITA) {this.f20RITA = f20RITA;}

    public BigInteger getF25RITA() {return f25RITA;}

    public void setF25RITA(BigInteger f25RITA) {this.f25RITA = f25RITA;}

    public BigInteger getF30RITA() {return f30RITA;}

    public void setF30RITA(BigInteger f30RITA) {this.f30RITA = f30RITA;}

    public BigInteger getF35RITA() {return f35RITA;}

    public void setF35RITA(BigInteger f35RITA) {this.f35RITA = f35RITA;}

    public BigInteger getF40RITA() {return f40RITA;}

    public void setF40RITA(BigInteger f40RITA) {this.f40RITA = f40RITA;}

    public BigInteger getF45RITA() {return f45RITA;}

    public void setF45RITA(BigInteger f45RITA) {this.f45RITA = f45RITA;}

    public BigInteger getF50RITA() {return f50RITA;}

    public void setF50RITA(BigInteger f50RITA) {this.f50RITA = f50RITA;}

    public BigInteger getMuRITA() {return muRITA;}

    public void setMuRITA(BigInteger muRITA) {this.muRITA = muRITA;}

    public BigInteger getM15RITA() {return m15RITA;}

    public void setM15RITA(BigInteger m15RITA) {this.m15RITA = m15RITA;}

    public BigInteger getM20RITA() {return m20RITA;}

    public void setM20RITA(BigInteger m20RITA) {this.m20RITA = m20RITA;}

    public BigInteger getM25RITA() {return m25RITA;}

    public void setM25RITA(BigInteger m25RITA) {this.m25RITA = m25RITA;}

    public BigInteger getM30RITA() {return m30RITA;}

    public void setM30RITA(BigInteger m30RITA) {this.m30RITA = m30RITA;}

    public BigInteger getM35RITA() {return m35RITA;}

    public void setM35RITA(BigInteger m35RITA) {this.m35RITA = m35RITA;}

    public BigInteger getM40RITA() {return m40RITA;}

    public void setM40RITA(BigInteger m40RITA) {this.m40RITA = m40RITA;}

    public BigInteger getM45RITA() {return m45RITA;}

    public void setM45RITA(BigInteger m45RITA) {this.m45RITA = m45RITA;}

    public BigInteger getM50RITA() {return m50RITA;}

    public void setM50RITA(BigInteger m50RITA) {this.m50RITA = m50RITA;}

    public Integer getTotalRITARecent() {
        totalRITARecent = 0;
        if (fuRITA != null) {
            totalRITARecent += fuRITA.intValue();
        }
        if (f15RITA != null) {
            totalRITARecent += f15RITA.intValue();
        }
        if (f20RITA != null) {
            totalRITARecent += f20RITA.intValue();
        }
        if (f25RITA != null) {
            totalRITARecent += f25RITA.intValue();
        }
        if (f30RITA != null) {
            totalRITARecent += f30RITA.intValue();
        }
        if (f35RITA != null) {
            totalRITARecent += f35RITA.intValue();
        }
        if (f40RITA != null) {
            totalRITARecent += f40RITA.intValue();
        }
        if (f45RITA != null) {
            totalRITARecent += f45RITA.intValue();
        }
        if (f50RITA != null) {
            totalRITARecent += f50RITA.intValue();
        }

        if (muRITA != null) {
            totalRITARecent += muRITA.intValue();
        }
        if (m15RITA != null) {
            totalRITARecent += m15RITA.intValue();
        }
        if (m20RITA != null) {
            totalRITARecent += m20RITA.intValue();
        }
        if (m25RITA != null) {
            totalRITARecent += m25RITA.intValue();
        }
        if (m30RITA != null) {
            totalRITARecent += m30RITA.intValue();
        }
        if (m35RITA != null) {
            totalRITARecent += m35RITA.intValue();
        }
        if (m40RITA != null) {
            totalRITARecent += m40RITA.intValue();
        }
        if (m45RITA != null) {
            totalRITARecent += m45RITA.intValue();
        }
        if (m50RITA != null) {
            totalRITARecent += m50RITA.intValue();
        }
        return totalRITARecent;
    }

    public void setTotalRITARecent(Integer totalRITARecent) {this.totalRITARecent = totalRITARecent;}

    public BigInteger getPwidRITALongTerm() {return pwidRITALongTerm;}

    public void setPwidRITALongTerm(BigInteger pwidRITALongTerm) {this.pwidRITALongTerm = pwidRITALongTerm;}

    public BigInteger getMsmRITALongTerm() {return msmRITALongTerm;}

    public void setMsmRITALongTerm(BigInteger msmRITALongTerm) {this.msmRITALongTerm = msmRITALongTerm;}

    public BigInteger getTgRITALongTerm() {return tgRITALongTerm;}

    public void setTgRITALongTerm(BigInteger tgRITALongTerm) {this.tgRITALongTerm = tgRITALongTerm;}

    public BigInteger getFswRITALongTerm() {return fswRITALongTerm;}

    public void setFswRITALongTerm(BigInteger fswRITALongTerm) {this.fswRITALongTerm = fswRITALongTerm;}

    public BigInteger getOtherRITALongTerm() {return otherRITALongTerm;}

    public void setOtherRITALongTerm(BigInteger otherRITALongTerm) {this.otherRITALongTerm = otherRITALongTerm;}

    public BigInteger getFuRITALongTerm() {return fuRITALongTerm;}

    public void setFuRITALongTerm(BigInteger fuRITALongTerm) {this.fuRITALongTerm = fuRITALongTerm;}

    public BigInteger getF15RITALongTerm() {return f15RITALongTerm;}

    public void setF15RITALongTerm(BigInteger f15RITALongTerm) {this.f15RITALongTerm = f15RITALongTerm;}

    public BigInteger getF20RITALongTerm() {return f20RITALongTerm;}

    public void setF20RITALongTerm(BigInteger f20RITALongTerm) {this.f20RITALongTerm = f20RITALongTerm;}

    public BigInteger getF25RITALongTerm() {return f25RITALongTerm;}

    public void setF25RITALongTerm(BigInteger f25RITALongTerm) {this.f25RITALongTerm = f25RITALongTerm;}

    public BigInteger getF30RITALongTerm() {return f30RITALongTerm;}

    public void setF30RITALongTerm(BigInteger f30RITALongTerm) {this.f30RITALongTerm = f30RITALongTerm;}

    public BigInteger getF35RITALongTerm() {return f35RITALongTerm;}

    public void setF35RITALongTerm(BigInteger f35RITALongTerm) {this.f35RITALongTerm = f35RITALongTerm;}

    public BigInteger getF40RITALongTerm() {return f40RITALongTerm;}

    public void setF40RITALongTerm(BigInteger f40RITALongTerm) {this.f40RITALongTerm = f40RITALongTerm;}

    public BigInteger getF45RITALongTerm() {return f45RITALongTerm;}

    public void setF45RITALongTerm(BigInteger f45RITALongTerm) {this.f45RITALongTerm = f45RITALongTerm;}

    public BigInteger getF50RITALongTerm() {return f50RITALongTerm;}

    public void setF50RITALongTerm(BigInteger f50RITALongTerm) {this.f50RITALongTerm = f50RITALongTerm;}

    public BigInteger getMuRITALongTerm() {return muRITALongTerm;}

    public void setMuRITALongTerm(BigInteger muRITALongTerm) {this.muRITALongTerm = muRITALongTerm;}

    public BigInteger getM15RITALongTerm() {return m15RITALongTerm;}

    public void setM15RITALongTerm(BigInteger m15RITALongTerm) {this.m15RITALongTerm = m15RITALongTerm;}

    public BigInteger getM20RITALongTerm() {return m20RITALongTerm;}

    public void setM20RITALongTerm(BigInteger m20RITALongTerm) {this.m20RITALongTerm = m20RITALongTerm;}

    public BigInteger getM25RITALongTerm() {return m25RITALongTerm;}

    public void setM25RITALongTerm(BigInteger m25RITALongTerm) {this.m25RITALongTerm = m25RITALongTerm;}

    public BigInteger getM30RITALongTerm() {return m30RITALongTerm;}

    public void setM30RITALongTerm(BigInteger m30RITALongTerm) {this.m30RITALongTerm = m30RITALongTerm;}

    public BigInteger getM35RITALongTerm() {return m35RITALongTerm;}

    public void setM35RITALongTerm(BigInteger m35RITALongTerm) {this.m35RITALongTerm = m35RITALongTerm;}

    public BigInteger getM40RITALongTerm() {return m40RITALongTerm;}

    public void setM40RITALongTerm(BigInteger m40RITALongTerm) {this.m40RITALongTerm = m40RITALongTerm;}

    public BigInteger getM45RITALongTerm() {return m45RITALongTerm;}

    public void setM45RITALongTerm(BigInteger m45RITALongTerm) {this.m45RITALongTerm = m45RITALongTerm;}

    public BigInteger getM50RITALongTerm() {return m50RITALongTerm;}

    public void setM50RITALongTerm(BigInteger m50RITALongTerm) {this.m50RITALongTerm = m50RITALongTerm;}

    public Integer getTotalRITALongTerm() {
        totalRITALongTerm = 0;
        if (fuRITALongTerm != null) {
            totalRITALongTerm += fuRITALongTerm.intValue();
        }
        if (f15RITALongTerm != null) {
            totalRITALongTerm += f15RITALongTerm.intValue();
        }
        if (f20RITALongTerm != null) {
            totalRITALongTerm += f20RITALongTerm.intValue();
        }
        if (f25RITALongTerm != null) {
            totalRITALongTerm += f25RITALongTerm.intValue();
        }
        if (f30RITALongTerm != null) {
            totalRITALongTerm += f30RITALongTerm.intValue();
        }
        if (f35RITALongTerm != null) {
            totalRITALongTerm += f35RITALongTerm.intValue();
        }
        if (f40RITALongTerm != null) {
            totalRITALongTerm += f40RITALongTerm.intValue();
        }
        if (f45RITALongTerm != null) {
            totalRITALongTerm += f45RITALongTerm.intValue();
        }
        if (f50RITALongTerm != null) {
            totalRITALongTerm += f50RITALongTerm.intValue();
        }

        if (muRITALongTerm != null) {
            totalRITALongTerm += muRITALongTerm.intValue();
        }
        if (m15RITALongTerm != null) {
            totalRITALongTerm += m15RITALongTerm.intValue();
        }
        if (m20RITALongTerm != null) {
            totalRITALongTerm += m20RITALongTerm.intValue();
        }
        if (m25RITALongTerm != null) {
            totalRITALongTerm += m25RITALongTerm.intValue();
        }
        if (m30RITALongTerm != null) {
            totalRITALongTerm += m30RITALongTerm.intValue();
        }
        if (m35RITALongTerm != null) {
            totalRITALongTerm += m35RITALongTerm.intValue();
        }
        if (m40RITALongTerm != null) {
            totalRITALongTerm += m40RITALongTerm.intValue();
        }
        if (m45RITALongTerm != null) {
            totalRITALongTerm += m45RITALongTerm.intValue();
        }
        if (m50RITALongTerm != null) {
            totalRITALongTerm += m50RITALongTerm.intValue();
        }
        return totalRITALongTerm;
    }

    public void setTotalRITALongTerm(Integer totalRITALongTerm) {this.totalRITALongTerm = totalRITALongTerm;}

    public Integer getTotalRITA() {
        totalRITA = this.getTotalRITALongTerm() + this.getTotalRITARecent();
        return totalRITA;
    }

    public void setTotalRITA(Integer totalRITA) {this.totalRITA = totalRITA;}
}
