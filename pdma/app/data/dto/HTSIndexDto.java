package org.pepfar.pdma.app.data.dto;

import java.math.BigInteger;

public class HTSIndexDto {
    private BigInteger orgId;
    private String orgCode;
    private String orgName;
    private String provinceName;
    private String districtName;
    private String support;
    private String modality;

    private BigInteger fuOffered = new BigInteger(String.valueOf(0));
    private BigInteger f0Offered = new BigInteger(String.valueOf(0));
    private BigInteger f1Offered = new BigInteger(String.valueOf(0));
    private BigInteger f5Offered = new BigInteger(String.valueOf(0));
    private BigInteger f10Offered = new BigInteger(String.valueOf(0));
    private BigInteger f15Offered = new BigInteger(String.valueOf(0));
    private BigInteger f20Offered = new BigInteger(String.valueOf(0));
    private BigInteger f25Offered = new BigInteger(String.valueOf(0));
    private BigInteger f30Offered = new BigInteger(String.valueOf(0));
    private BigInteger f35Offered = new BigInteger(String.valueOf(0));
    private BigInteger f40Offered = new BigInteger(String.valueOf(0));
    private BigInteger f45Offered = new BigInteger(String.valueOf(0));
    private BigInteger f50Offered = new BigInteger(String.valueOf(0));

    private BigInteger muOffered = new BigInteger(String.valueOf(0));
    private BigInteger m0Offered = new BigInteger(String.valueOf(0));
    private BigInteger m1Offered = new BigInteger(String.valueOf(0));
    private BigInteger m5Offered = new BigInteger(String.valueOf(0));
    private BigInteger m10Offered = new BigInteger(String.valueOf(0));
    private BigInteger m15Offered = new BigInteger(String.valueOf(0));
    private BigInteger m20Offered = new BigInteger(String.valueOf(0));
    private BigInteger m25Offered = new BigInteger(String.valueOf(0));
    private BigInteger m30Offered = new BigInteger(String.valueOf(0));
    private BigInteger m35Offered = new BigInteger(String.valueOf(0));
    private BigInteger m40Offered = new BigInteger(String.valueOf(0));
    private BigInteger m45Offered = new BigInteger(String.valueOf(0));
    private BigInteger m50Offered = new BigInteger(String.valueOf(0));

    private Integer totalOffered;

    private BigInteger fuAccepted = new BigInteger(String.valueOf(0));
    private BigInteger f0Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f1Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f5Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f10Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f15Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f20Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f25Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f30Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f35Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f40Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f45Accepted = new BigInteger(String.valueOf(0));
    private BigInteger f50Accepted = new BigInteger(String.valueOf(0));

    private BigInteger muAccepted = new BigInteger(String.valueOf(0));
    private BigInteger m0Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m1Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m5Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m10Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m15Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m20Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m25Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m30Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m35Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m40Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m45Accepted = new BigInteger(String.valueOf(0));
    private BigInteger m50Accepted = new BigInteger(String.valueOf(0));

    private Integer totalAccepted;

    private BigInteger fuContactsElicited = new BigInteger(String.valueOf(0));
    private BigInteger f0ContactsElicited = new BigInteger(String.valueOf(0));
    private BigInteger f15ContactsElicited = new BigInteger(String.valueOf(0));

    private BigInteger muContactsElicited = new BigInteger(String.valueOf(0));
    private BigInteger m0ContactsElicited = new BigInteger(String.valueOf(0));
    private BigInteger m15ContactsElicited = new BigInteger(String.valueOf(0));

    private Integer totalContactsElicited;

    private BigInteger fuKnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f0KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f1KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f5KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f10KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f15KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f20KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f25KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f30KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f35KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f40KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f45KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger f50KnownPositives = new BigInteger(String.valueOf(0));

    private BigInteger muKnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m0KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m1KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m5KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m10KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m15KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m20KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m25KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m30KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m35KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m40KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m45KnownPositives = new BigInteger(String.valueOf(0));
    private BigInteger m50KnownPositives = new BigInteger(String.valueOf(0));

    private Integer totalKnownPositives;

    private BigInteger f1DocumentedNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f5DocumentedNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f10DocumentedNegatives = new BigInteger(String.valueOf(0));

    private BigInteger m1DocumentedNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m5DocumentedNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m10DocumentedNegatives = new BigInteger(String.valueOf(0));

    private Integer totalDocumentedNegatives;

    private BigInteger fuNewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f0NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f1NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f5NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f10NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f15NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f20NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f25NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f30NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f35NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f40NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f45NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger f50NewPositives = new BigInteger(String.valueOf(0));

    private BigInteger muNewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m0NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m1NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m5NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m10NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m15NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m20NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m25NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m30NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m35NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m40NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m45NewPositives = new BigInteger(String.valueOf(0));
    private BigInteger m50NewPositives = new BigInteger(String.valueOf(0));

    private Integer totalNewPositives;

    private BigInteger fuNewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f0NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f1NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f5NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f10NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f15NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f20NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f25NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f30NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f35NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f40NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f45NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger f50NewNegatives = new BigInteger(String.valueOf(0));

    private BigInteger muNewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m0NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m1NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m5NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m10NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m15NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m20NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m25NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m30NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m35NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m40NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m45NewNegatives = new BigInteger(String.valueOf(0));
    private BigInteger m50NewNegatives = new BigInteger(String.valueOf(0));

    private Integer totalNewNegatives;

    private Integer totalContactsTested;

    public BigInteger getOrgId() {return orgId;}

    public void setOrgId(BigInteger orgId) {this.orgId = orgId;}

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

    public BigInteger getFuOffered() {return fuOffered;}

    public void setFuOffered(BigInteger fuOffered) {this.fuOffered = fuOffered;}

    public BigInteger getF0Offered() {return f0Offered;}

    public void setF0Offered(BigInteger f0Offered) {this.f0Offered = f0Offered;}

    public BigInteger getF1Offered() {return f1Offered;}

    public void setF1Offered(BigInteger f1Offered) {this.f1Offered = f1Offered;}

    public BigInteger getF5Offered() {return f5Offered;}

    public void setF5Offered(BigInteger f5Offered) {this.f5Offered = f5Offered;}

    public BigInteger getF10Offered() {return f10Offered;}

    public void setF10Offered(BigInteger f10Offered) {this.f10Offered = f10Offered;}

    public BigInteger getF15Offered() {return f15Offered;}

    public void setF15Offered(BigInteger f15Offered) {this.f15Offered = f15Offered;}

    public BigInteger getF20Offered() {return f20Offered;}

    public void setF20Offered(BigInteger f20Offered) {this.f20Offered = f20Offered;}

    public BigInteger getF25Offered() {return f25Offered;}

    public void setF25Offered(BigInteger f25Offered) {this.f25Offered = f25Offered;}

    public BigInteger getF30Offered() {return f30Offered;}

    public void setF30Offered(BigInteger f30Offered) {this.f30Offered = f30Offered;}

    public BigInteger getF35Offered() {return f35Offered;}

    public void setF35Offered(BigInteger f35Offered) {this.f35Offered = f35Offered;}

    public BigInteger getF40Offered() {return f40Offered;}

    public void setF40Offered(BigInteger f40Offered) {this.f40Offered = f40Offered;}

    public BigInteger getF45Offered() {return f45Offered;}

    public void setF45Offered(BigInteger f45Offered) {this.f45Offered = f45Offered;}

    public BigInteger getF50Offered() {return f50Offered;}

    public void setF50Offered(BigInteger f50Offered) {this.f50Offered = f50Offered;}

    public BigInteger getMuOffered() {return muOffered;}

    public void setMuOffered(BigInteger muOffered) {this.muOffered = muOffered;}

    public BigInteger getM0Offered() {return m0Offered;}

    public void setM0Offered(BigInteger m0Offered) {this.m0Offered = m0Offered;}

    public BigInteger getM1Offered() {return m1Offered;}

    public void setM1Offered(BigInteger m1Offered) {this.m1Offered = m1Offered;}

    public BigInteger getM5Offered() {return m5Offered;}

    public void setM5Offered(BigInteger m5Offered) {this.m5Offered = m5Offered;}

    public BigInteger getM10Offered() {return m10Offered;}

    public void setM10Offered(BigInteger m10Offered) {this.m10Offered = m10Offered;}

    public BigInteger getM15Offered() {return m15Offered;}

    public void setM15Offered(BigInteger m15Offered) {this.m15Offered = m15Offered;}

    public BigInteger getM20Offered() {return m20Offered;}

    public void setM20Offered(BigInteger m20Offered) {this.m20Offered = m20Offered;}

    public BigInteger getM25Offered() {return m25Offered;}

    public void setM25Offered(BigInteger m25Offered) {this.m25Offered = m25Offered;}

    public BigInteger getM30Offered() {return m30Offered;}

    public void setM30Offered(BigInteger m30Offered) {this.m30Offered = m30Offered;}

    public BigInteger getM35Offered() {return m35Offered;}

    public void setM35Offered(BigInteger m35Offered) {this.m35Offered = m35Offered;}

    public BigInteger getM40Offered() {return m40Offered;}

    public void setM40Offered(BigInteger m40Offered) {this.m40Offered = m40Offered;}

    public BigInteger getM45Offered() {return m45Offered;}

    public void setM45Offered(BigInteger m45Offered) {this.m45Offered = m45Offered;}

    public BigInteger getM50Offered() {return m50Offered;}

    public void setM50Offered(BigInteger m50Offered) {this.m50Offered = m50Offered;}

    public Integer getTotalOffered() {
        totalOffered = 0;
        if (fuOffered != null) {
            totalOffered += fuOffered.intValue();
        }
        if (f0Offered != null) {
            totalOffered += f0Offered.intValue();
        }
        if (f1Offered != null) {
            totalOffered += f1Offered.intValue();
        }
        if (f5Offered != null) {
            totalOffered += f5Offered.intValue();
        }
        if (f10Offered != null) {
            totalOffered += f10Offered.intValue();
        }
        if (f15Offered != null) {
            totalOffered += f15Offered.intValue();
        }
        if (f20Offered != null) {
            totalOffered += f20Offered.intValue();
        }
        if (f25Offered != null) {
            totalOffered += f25Offered.intValue();
        }
        if (f30Offered != null) {
            totalOffered += f30Offered.intValue();
        }
        if (f35Offered != null) {
            totalOffered += f35Offered.intValue();
        }
        if (f40Offered != null) {
            totalOffered += f40Offered.intValue();
        }
        if (f45Offered != null) {
            totalOffered += f45Offered.intValue();
        }
        if (f50Offered != null) {
            totalOffered += f50Offered.intValue();
        }

        if (muOffered != null) {
            totalOffered += muOffered.intValue();
        }
        if (m0Offered != null) {
            totalOffered += m0Offered.intValue();
        }
        if (m1Offered != null) {
            totalOffered += m1Offered.intValue();
        }
        if (m5Offered != null) {
            totalOffered += m5Offered.intValue();
        }
        if (m10Offered != null) {
            totalOffered += m10Offered.intValue();
        }
        if (m15Offered != null) {
            totalOffered += m15Offered.intValue();
        }
        if (m20Offered != null) {
            totalOffered += m20Offered.intValue();
        }
        if (m25Offered != null) {
            totalOffered += m25Offered.intValue();
        }
        if (m30Offered != null) {
            totalOffered += m30Offered.intValue();
        }
        if (m35Offered != null) {
            totalOffered += m35Offered.intValue();
        }
        if (m40Offered != null) {
            totalOffered += m40Offered.intValue();
        }
        if (m45Offered != null) {
            totalOffered += m45Offered.intValue();
        }
        if (m50Offered != null) {
            totalOffered += m50Offered.intValue();
        }
        return totalOffered;
    }

    public void setTotalOffered(Integer totalOffered) {this.totalOffered = totalOffered;}

    public BigInteger getFuAccepted() {return fuAccepted;}

    public void setFuAccepted(BigInteger fuAccepted) {this.fuAccepted = fuAccepted;}

    public BigInteger getF0Accepted() {return f0Accepted;}

    public void setF0Accepted(BigInteger f0Accepted) {this.f0Accepted = f0Accepted;}

    public BigInteger getF1Accepted() {return f1Accepted;}

    public void setF1Accepted(BigInteger f1Accepted) {this.f1Accepted = f1Accepted;}

    public BigInteger getF5Accepted() {return f5Accepted;}

    public void setF5Accepted(BigInteger f5Accepted) {this.f5Accepted = f5Accepted;}

    public BigInteger getF10Accepted() {return f10Accepted;}

    public void setF10Accepted(BigInteger f10Accepted) {this.f10Accepted = f10Accepted;}

    public BigInteger getF15Accepted() {return f15Accepted;}

    public void setF15Accepted(BigInteger f15Accepted) {this.f15Accepted = f15Accepted;}

    public BigInteger getF20Accepted() {return f20Accepted;}

    public void setF20Accepted(BigInteger f20Accepted) {this.f20Accepted = f20Accepted;}

    public BigInteger getF25Accepted() {return f25Accepted;}

    public void setF25Accepted(BigInteger f25Accepted) {this.f25Accepted = f25Accepted;}

    public BigInteger getF30Accepted() {return f30Accepted;}

    public void setF30Accepted(BigInteger f30Accepted) {this.f30Accepted = f30Accepted;}

    public BigInteger getF35Accepted() {return f35Accepted;}

    public void setF35Accepted(BigInteger f35Accepted) {this.f35Accepted = f35Accepted;}

    public BigInteger getF40Accepted() {return f40Accepted;}

    public void setF40Accepted(BigInteger f40Accepted) {this.f40Accepted = f40Accepted;}

    public BigInteger getF45Accepted() {return f45Accepted;}

    public void setF45Accepted(BigInteger f45Accepted) {this.f45Accepted = f45Accepted;}

    public BigInteger getF50Accepted() {return f50Accepted;}

    public void setF50Accepted(BigInteger f50Accepted) {this.f50Accepted = f50Accepted;}

    public BigInteger getMuAccepted() {return muAccepted;}

    public void setMuAccepted(BigInteger muAccepted) {this.muAccepted = muAccepted;}

    public BigInteger getM0Accepted() {return m0Accepted;}

    public void setM0Accepted(BigInteger m0Accepted) {this.m0Accepted = m0Accepted;}

    public BigInteger getM1Accepted() {return m1Accepted;}

    public void setM1Accepted(BigInteger m1Accepted) {this.m1Accepted = m1Accepted;}

    public BigInteger getM5Accepted() {return m5Accepted;}

    public void setM5Accepted(BigInteger m5Accepted) {this.m5Accepted = m5Accepted;}

    public BigInteger getM10Accepted() {return m10Accepted;}

    public void setM10Accepted(BigInteger m10Accepted) {this.m10Accepted = m10Accepted;}

    public BigInteger getM15Accepted() {return m15Accepted;}

    public void setM15Accepted(BigInteger m15Accepted) {this.m15Accepted = m15Accepted;}

    public BigInteger getM20Accepted() {return m20Accepted;}

    public void setM20Accepted(BigInteger m20Accepted) {this.m20Accepted = m20Accepted;}

    public BigInteger getM25Accepted() {return m25Accepted;}

    public void setM25Accepted(BigInteger m25Accepted) {this.m25Accepted = m25Accepted;}

    public BigInteger getM30Accepted() {return m30Accepted;}

    public void setM30Accepted(BigInteger m30Accepted) {this.m30Accepted = m30Accepted;}

    public BigInteger getM35Accepted() {return m35Accepted;}

    public void setM35Accepted(BigInteger m35Accepted) {this.m35Accepted = m35Accepted;}

    public BigInteger getM40Accepted() {return m40Accepted;}

    public void setM40Accepted(BigInteger m40Accepted) {this.m40Accepted = m40Accepted;}

    public BigInteger getM45Accepted() {return m45Accepted;}

    public void setM45Accepted(BigInteger m45Accepted) {this.m45Accepted = m45Accepted;}

    public BigInteger getM50Accepted() {return m50Accepted;}

    public void setM50Accepted(BigInteger m50Accepted) {this.m50Accepted = m50Accepted;}

    public Integer getTotalAccepted() {
        totalAccepted = 0;
        if (fuAccepted != null) {
            totalAccepted += fuAccepted.intValue();
        }
        if (f0Accepted != null) {
            totalAccepted += f0Accepted.intValue();
        }
        if (f1Accepted != null) {
            totalAccepted += f1Accepted.intValue();
        }
        if (f5Accepted != null) {
            totalAccepted += f5Accepted.intValue();
        }
        if (f10Accepted != null) {
            totalAccepted += f10Accepted.intValue();
        }
        if (f15Accepted != null) {
            totalAccepted += f15Accepted.intValue();
        }
        if (f20Accepted != null) {
            totalAccepted += f20Accepted.intValue();
        }
        if (f25Accepted != null) {
            totalAccepted += f25Accepted.intValue();
        }
        if (f30Accepted != null) {
            totalAccepted += f30Accepted.intValue();
        }
        if (f35Accepted != null) {
            totalAccepted += f35Accepted.intValue();
        }
        if (f40Accepted != null) {
            totalAccepted += f40Accepted.intValue();
        }
        if (f45Accepted != null) {
            totalAccepted += f45Accepted.intValue();
        }
        if (f50Accepted != null) {
            totalAccepted += f50Accepted.intValue();
        }

        if (muAccepted != null) {
            totalAccepted += muAccepted.intValue();
        }
        if (m0Accepted != null) {
            totalAccepted += m0Accepted.intValue();
        }
        if (m1Accepted != null) {
            totalAccepted += m1Accepted.intValue();
        }
        if (m5Accepted != null) {
            totalAccepted += m5Accepted.intValue();
        }
        if (m10Accepted != null) {
            totalAccepted += m10Accepted.intValue();
        }
        if (m15Accepted != null) {
            totalAccepted += m15Accepted.intValue();
        }
        if (m20Accepted != null) {
            totalAccepted += m20Accepted.intValue();
        }
        if (m25Accepted != null) {
            totalAccepted += m25Accepted.intValue();
        }
        if (m30Accepted != null) {
            totalAccepted += m30Accepted.intValue();
        }
        if (m35Accepted != null) {
            totalAccepted += m35Accepted.intValue();
        }
        if (m40Accepted != null) {
            totalAccepted += m40Accepted.intValue();
        }
        if (m45Accepted != null) {
            totalAccepted += m45Accepted.intValue();
        }
        if (m50Accepted != null) {
            totalAccepted += m50Accepted.intValue();
        }
        return totalAccepted;
    }

    public void setTotalAccepted(Integer totalAccepted) {this.totalAccepted = totalAccepted;}

    public BigInteger getFuContactsElicited() {return fuContactsElicited;}

    public void setFuContactsElicited(BigInteger fuContactsElicited) {this.fuContactsElicited = fuContactsElicited;}

    public BigInteger getF0ContactsElicited() {return f0ContactsElicited;}

    public void setF0ContactsElicited(BigInteger f0ContactsElicited) {this.f0ContactsElicited = f0ContactsElicited;}

    public BigInteger getF15ContactsElicited() {return f15ContactsElicited;}

    public void setF15ContactsElicited(BigInteger f15ContactsElicited) {this.f15ContactsElicited = f15ContactsElicited;}

    public BigInteger getMuContactsElicited() {return muContactsElicited;}

    public void setMuContactsElicited(BigInteger muContactsElicited) {this.muContactsElicited = muContactsElicited;}

    public BigInteger getM0ContactsElicited() {return m0ContactsElicited;}

    public void setM0ContactsElicited(BigInteger m0ContactsElicited) {this.m0ContactsElicited = m0ContactsElicited;}

    public BigInteger getM15ContactsElicited() {return m15ContactsElicited;}

    public void setM15ContactsElicited(BigInteger m15ContactsElicited) {this.m15ContactsElicited = m15ContactsElicited;}

    public Integer getTotalContactsElicited() {
        totalContactsElicited = 0;
        if (fuContactsElicited != null) {
            totalContactsElicited += fuContactsElicited.intValue();
        }
        if (f0ContactsElicited != null) {
            totalContactsElicited += f0ContactsElicited.intValue();
        }
        if (f15ContactsElicited != null) {
            totalContactsElicited += f15ContactsElicited.intValue();
        }

        if (muContactsElicited != null) {
            totalContactsElicited += muContactsElicited.intValue();
        }
        if (m0ContactsElicited != null) {
            totalContactsElicited += m0ContactsElicited.intValue();
        }
        if (m15ContactsElicited != null) {
            totalContactsElicited += m15ContactsElicited.intValue();
        }
        return totalContactsElicited;
    }

    public void setTotalContactsElicited(Integer totalContactsElicited) {this.totalContactsElicited = totalContactsElicited;}

    public BigInteger getFuKnownPositives() {return fuKnownPositives;}

    public void setFuKnownPositives(BigInteger fuKnownPositives) {this.fuKnownPositives = fuKnownPositives;}

    public BigInteger getF0KnownPositives() {return f0KnownPositives;}

    public void setF0KnownPositives(BigInteger f0KnownPositives) {this.f0KnownPositives = f0KnownPositives;}

    public BigInteger getF1KnownPositives() {return f1KnownPositives;}

    public void setF1KnownPositives(BigInteger f1KnownPositives) {this.f1KnownPositives = f1KnownPositives;}

    public BigInteger getF5KnownPositives() {return f5KnownPositives;}

    public void setF5KnownPositives(BigInteger f5KnownPositives) {this.f5KnownPositives = f5KnownPositives;}

    public BigInteger getF10KnownPositives() {return f10KnownPositives;}

    public void setF10KnownPositives(BigInteger f10KnownPositives) {this.f10KnownPositives = f10KnownPositives;}

    public BigInteger getF15KnownPositives() {return f15KnownPositives;}

    public void setF15KnownPositives(BigInteger f15KnownPositives) {this.f15KnownPositives = f15KnownPositives;}

    public BigInteger getF20KnownPositives() {return f20KnownPositives;}

    public void setF20KnownPositives(BigInteger f20KnownPositives) {this.f20KnownPositives = f20KnownPositives;}

    public BigInteger getF25KnownPositives() {return f25KnownPositives;}

    public void setF25KnownPositives(BigInteger f25KnownPositives) {this.f25KnownPositives = f25KnownPositives;}

    public BigInteger getF30KnownPositives() {return f30KnownPositives;}

    public void setF30KnownPositives(BigInteger f30KnownPositives) {this.f30KnownPositives = f30KnownPositives;}

    public BigInteger getF35KnownPositives() {return f35KnownPositives;}

    public void setF35KnownPositives(BigInteger f35KnownPositives) {this.f35KnownPositives = f35KnownPositives;}

    public BigInteger getF40KnownPositives() {return f40KnownPositives;}

    public void setF40KnownPositives(BigInteger f40KnownPositives) {this.f40KnownPositives = f40KnownPositives;}

    public BigInteger getF45KnownPositives() {return f45KnownPositives;}

    public void setF45KnownPositives(BigInteger f45KnownPositives) {this.f45KnownPositives = f45KnownPositives;}

    public BigInteger getF50KnownPositives() {return f50KnownPositives;}

    public void setF50KnownPositives(BigInteger f50KnownPositives) {this.f50KnownPositives = f50KnownPositives;}

    public BigInteger getMuKnownPositives() {return muKnownPositives;}

    public void setMuKnownPositives(BigInteger muKnownPositives) {this.muKnownPositives = muKnownPositives;}

    public BigInteger getM0KnownPositives() {return m0KnownPositives;}

    public void setM0KnownPositives(BigInteger m0KnownPositives) {this.m0KnownPositives = m0KnownPositives;}

    public BigInteger getM1KnownPositives() {return m1KnownPositives;}

    public void setM1KnownPositives(BigInteger m1KnownPositives) {this.m1KnownPositives = m1KnownPositives;}

    public BigInteger getM5KnownPositives() {return m5KnownPositives;}

    public void setM5KnownPositives(BigInteger m5KnownPositives) {this.m5KnownPositives = m5KnownPositives;}

    public BigInteger getM10KnownPositives() {return m10KnownPositives;}

    public void setM10KnownPositives(BigInteger m10KnownPositives) {this.m10KnownPositives = m10KnownPositives;}

    public BigInteger getM15KnownPositives() {return m15KnownPositives;}

    public void setM15KnownPositives(BigInteger m15KnownPositives) {this.m15KnownPositives = m15KnownPositives;}

    public BigInteger getM20KnownPositives() {return m20KnownPositives;}

    public void setM20KnownPositives(BigInteger m20KnownPositives) {this.m20KnownPositives = m20KnownPositives;}

    public BigInteger getM25KnownPositives() {return m25KnownPositives;}

    public void setM25KnownPositives(BigInteger m25KnownPositives) {this.m25KnownPositives = m25KnownPositives;}

    public BigInteger getM30KnownPositives() {return m30KnownPositives;}

    public void setM30KnownPositives(BigInteger m30KnownPositives) {this.m30KnownPositives = m30KnownPositives;}

    public BigInteger getM35KnownPositives() {return m35KnownPositives;}

    public void setM35KnownPositives(BigInteger m35KnownPositives) {this.m35KnownPositives = m35KnownPositives;}

    public BigInteger getM40KnownPositives() {return m40KnownPositives;}

    public void setM40KnownPositives(BigInteger m40KnownPositives) {this.m40KnownPositives = m40KnownPositives;}

    public BigInteger getM45KnownPositives() {return m45KnownPositives;}

    public void setM45KnownPositives(BigInteger m45KnownPositives) {this.m45KnownPositives = m45KnownPositives;}

    public BigInteger getM50KnownPositives() {return m50KnownPositives;}

    public void setM50KnownPositives(BigInteger m50KnownPositives) {this.m50KnownPositives = m50KnownPositives;}

    public Integer getTotalKnownPositives() {
        totalKnownPositives = 0;
        if (fuKnownPositives != null) {
            totalKnownPositives += fuKnownPositives.intValue();
        }
        if (f0KnownPositives != null) {
            totalKnownPositives += f0KnownPositives.intValue();
        }
        if (f1KnownPositives != null) {
            totalKnownPositives += f1KnownPositives.intValue();
        }
        if (f5KnownPositives != null) {
            totalKnownPositives += f5KnownPositives.intValue();
        }
        if (f10KnownPositives != null) {
            totalKnownPositives += f10KnownPositives.intValue();
        }
        if (f15KnownPositives != null) {
            totalKnownPositives += f15KnownPositives.intValue();
        }
        if (f20KnownPositives != null) {
            totalKnownPositives += f20KnownPositives.intValue();
        }
        if (f25KnownPositives != null) {
            totalKnownPositives += f25KnownPositives.intValue();
        }
        if (f30KnownPositives != null) {
            totalKnownPositives += f30KnownPositives.intValue();
        }
        if (f35KnownPositives != null) {
            totalKnownPositives += f35KnownPositives.intValue();
        }
        if (f40KnownPositives != null) {
            totalKnownPositives += f40KnownPositives.intValue();
        }
        if (f45KnownPositives != null) {
            totalKnownPositives += f45KnownPositives.intValue();
        }
        if (f50KnownPositives != null) {
            totalKnownPositives += f50KnownPositives.intValue();
        }

        if (muKnownPositives != null) {
            totalKnownPositives += muKnownPositives.intValue();
        }
        if (m0KnownPositives != null) {
            totalKnownPositives += m0KnownPositives.intValue();
        }
        if (m1KnownPositives != null) {
            totalKnownPositives += m1KnownPositives.intValue();
        }
        if (m5KnownPositives != null) {
            totalKnownPositives += m5KnownPositives.intValue();
        }
        if (m10KnownPositives != null) {
            totalKnownPositives += m10KnownPositives.intValue();
        }
        if (m15KnownPositives != null) {
            totalKnownPositives += m15KnownPositives.intValue();
        }
        if (m20KnownPositives != null) {
            totalKnownPositives += m20KnownPositives.intValue();
        }
        if (m25KnownPositives != null) {
            totalKnownPositives += m25KnownPositives.intValue();
        }
        if (m30KnownPositives != null) {
            totalKnownPositives += m30KnownPositives.intValue();
        }
        if (m35KnownPositives != null) {
            totalKnownPositives += m35KnownPositives.intValue();
        }
        if (m40KnownPositives != null) {
            totalKnownPositives += m40KnownPositives.intValue();
        }
        if (m45KnownPositives != null) {
            totalKnownPositives += m45KnownPositives.intValue();
        }
        if (m50KnownPositives != null) {
            totalKnownPositives += m50KnownPositives.intValue();
        }
        return totalKnownPositives;
    }

    public void setTotalKnownPositives(Integer totalKnownPositives) {this.totalKnownPositives = totalKnownPositives;}

    public BigInteger getF1DocumentedNegatives() {return f1DocumentedNegatives;}

    public void setF1DocumentedNegatives(BigInteger f1DocumentedNegatives) {this.f1DocumentedNegatives = f1DocumentedNegatives;}

    public BigInteger getF5DocumentedNegatives() {return f5DocumentedNegatives;}

    public void setF5DocumentedNegatives(BigInteger f5DocumentedNegatives) {this.f5DocumentedNegatives = f5DocumentedNegatives;}

    public BigInteger getF10DocumentedNegatives() {return f10DocumentedNegatives;}

    public void setF10DocumentedNegatives(BigInteger f10DocumentedNegatives) {this.f10DocumentedNegatives = f10DocumentedNegatives;}

    public BigInteger getM1DocumentedNegatives() {return m1DocumentedNegatives;}

    public void setM1DocumentedNegatives(BigInteger m1DocumentedNegatives) {this.m1DocumentedNegatives = m1DocumentedNegatives;}

    public BigInteger getM5DocumentedNegatives() {return m5DocumentedNegatives;}

    public void setM5DocumentedNegatives(BigInteger m5DocumentedNegatives) {this.m5DocumentedNegatives = m5DocumentedNegatives;}

    public BigInteger getM10DocumentedNegatives() {return m10DocumentedNegatives;}

    public void setM10DocumentedNegatives(BigInteger m10DocumentedNegatives) {this.m10DocumentedNegatives = m10DocumentedNegatives;}

    public Integer getTotalDocumentedNegatives() {
        totalDocumentedNegatives = 0;
        if (f1DocumentedNegatives != null) {
            totalDocumentedNegatives += f1DocumentedNegatives.intValue();
        }
        if (f5DocumentedNegatives != null) {
            totalDocumentedNegatives += f5DocumentedNegatives.intValue();
        }
        if (f10DocumentedNegatives != null) {
            totalDocumentedNegatives += f10DocumentedNegatives.intValue();
        }

        if (m1DocumentedNegatives != null) {
            totalDocumentedNegatives += m1DocumentedNegatives.intValue();
        }
        if (m5DocumentedNegatives != null) {
            totalDocumentedNegatives += m5DocumentedNegatives.intValue();
        }
        if (m10DocumentedNegatives != null) {
            totalDocumentedNegatives += m10DocumentedNegatives.intValue();
        }
        return totalDocumentedNegatives;
    }

    public void setTotalDocumentedNegatives(Integer totalDocumentedNegatives) {this.totalDocumentedNegatives = totalDocumentedNegatives;}

    public BigInteger getFuNewPositives() {return fuNewPositives;}

    public void setFuNewPositives(BigInteger fuNewPositives) {this.fuNewPositives = fuNewPositives;}

    public BigInteger getF0NewPositives() {return f0NewPositives;}

    public void setF0NewPositives(BigInteger f0NewPositives) {this.f0NewPositives = f0NewPositives;}

    public BigInteger getF1NewPositives() {return f1NewPositives;}

    public void setF1NewPositives(BigInteger f1NewPositives) {this.f1NewPositives = f1NewPositives;}

    public BigInteger getF5NewPositives() {return f5NewPositives;}

    public void setF5NewPositives(BigInteger f5NewPositives) {this.f5NewPositives = f5NewPositives;}

    public BigInteger getF10NewPositives() {return f10NewPositives;}

    public void setF10NewPositives(BigInteger f10NewPositives) {this.f10NewPositives = f10NewPositives;}

    public BigInteger getF15NewPositives() {return f15NewPositives;}

    public void setF15NewPositives(BigInteger f15NewPositives) {this.f15NewPositives = f15NewPositives;}

    public BigInteger getF20NewPositives() {return f20NewPositives;}

    public void setF20NewPositives(BigInteger f20NewPositives) {this.f20NewPositives = f20NewPositives;}

    public BigInteger getF25NewPositives() {return f25NewPositives;}

    public void setF25NewPositives(BigInteger f25NewPositives) {this.f25NewPositives = f25NewPositives;}

    public BigInteger getF30NewPositives() {return f30NewPositives;}

    public void setF30NewPositives(BigInteger f30NewPositives) {this.f30NewPositives = f30NewPositives;}

    public BigInteger getF35NewPositives() {return f35NewPositives;}

    public void setF35NewPositives(BigInteger f35NewPositives) {this.f35NewPositives = f35NewPositives;}

    public BigInteger getF40NewPositives() {return f40NewPositives;}

    public void setF40NewPositives(BigInteger f40NewPositives) {this.f40NewPositives = f40NewPositives;}

    public BigInteger getF45NewPositives() {return f45NewPositives;}

    public void setF45NewPositives(BigInteger f45NewPositives) {this.f45NewPositives = f45NewPositives;}

    public BigInteger getF50NewPositives() {return f50NewPositives;}

    public void setF50NewPositives(BigInteger f50NewPositives) {this.f50NewPositives = f50NewPositives;}

    public BigInteger getMuNewPositives() {return muNewPositives;}

    public void setMuNewPositives(BigInteger muNewPositives) {this.muNewPositives = muNewPositives;}

    public BigInteger getM0NewPositives() {return m0NewPositives;}

    public void setM0NewPositives(BigInteger m0NewPositives) {this.m0NewPositives = m0NewPositives;}

    public BigInteger getM1NewPositives() {return m1NewPositives;}

    public void setM1NewPositives(BigInteger m1NewPositives) {this.m1NewPositives = m1NewPositives;}

    public BigInteger getM5NewPositives() {return m5NewPositives;}

    public void setM5NewPositives(BigInteger m5NewPositives) {this.m5NewPositives = m5NewPositives;}

    public BigInteger getM10NewPositives() {return m10NewPositives;}

    public void setM10NewPositives(BigInteger m10NewPositives) {this.m10NewPositives = m10NewPositives;}

    public BigInteger getM15NewPositives() {return m15NewPositives;}

    public void setM15NewPositives(BigInteger m15NewPositives) {this.m15NewPositives = m15NewPositives;}

    public BigInteger getM20NewPositives() {return m20NewPositives;}

    public void setM20NewPositives(BigInteger m20NewPositives) {this.m20NewPositives = m20NewPositives;}

    public BigInteger getM25NewPositives() {return m25NewPositives;}

    public void setM25NewPositives(BigInteger m25NewPositives) {this.m25NewPositives = m25NewPositives;}

    public BigInteger getM30NewPositives() {return m30NewPositives;}

    public void setM30NewPositives(BigInteger m30NewPositives) {this.m30NewPositives = m30NewPositives;}

    public BigInteger getM35NewPositives() {return m35NewPositives;}

    public void setM35NewPositives(BigInteger m35NewPositives) {this.m35NewPositives = m35NewPositives;}

    public BigInteger getM40NewPositives() {return m40NewPositives;}

    public void setM40NewPositives(BigInteger m40NewPositives) {this.m40NewPositives = m40NewPositives;}

    public BigInteger getM45NewPositives() {return m45NewPositives;}

    public void setM45NewPositives(BigInteger m45NewPositives) {this.m45NewPositives = m45NewPositives;}

    public BigInteger getM50NewPositives() {return m50NewPositives;}

    public void setM50NewPositives(BigInteger m50NewPositives) {this.m50NewPositives = m50NewPositives;}

    public Integer getTotalNewPositives() {
        totalNewPositives = 0;
        if (fuNewPositives != null) {
            totalNewPositives += fuNewPositives.intValue();
        }
        if (f0NewPositives != null) {
            totalNewPositives += f0NewPositives.intValue();
        }
        if (f1NewPositives != null) {
            totalNewPositives += f1NewPositives.intValue();
        }
        if (f5NewPositives != null) {
            totalNewPositives += f5NewPositives.intValue();
        }
        if (f10NewPositives != null) {
            totalNewPositives += f10NewPositives.intValue();
        }
        if (f15NewPositives != null) {
            totalNewPositives += f15NewPositives.intValue();
        }
        if (f20NewPositives != null) {
            totalNewPositives += f20NewPositives.intValue();
        }
        if (f25NewPositives != null) {
            totalNewPositives += f25NewPositives.intValue();
        }
        if (f30NewPositives != null) {
            totalNewPositives += f30NewPositives.intValue();
        }
        if (f35NewPositives != null) {
            totalNewPositives += f35NewPositives.intValue();
        }
        if (f40NewPositives != null) {
            totalNewPositives += f40NewPositives.intValue();
        }
        if (f45NewPositives != null) {
            totalNewPositives += f45NewPositives.intValue();
        }
        if (f50NewPositives != null) {
            totalNewPositives += f50NewPositives.intValue();
        }

        if (muNewPositives != null) {
            totalNewPositives += muNewPositives.intValue();
        }
        if (m0NewPositives != null) {
            totalNewPositives += m0NewPositives.intValue();
        }
        if (m1NewPositives != null) {
            totalNewPositives += m1NewPositives.intValue();
        }
        if (m5NewPositives != null) {
            totalNewPositives += m5NewPositives.intValue();
        }
        if (m10NewPositives != null) {
            totalNewPositives += m10NewPositives.intValue();
        }
        if (m15NewPositives != null) {
            totalNewPositives += m15NewPositives.intValue();
        }
        if (m20NewPositives != null) {
            totalNewPositives += m20NewPositives.intValue();
        }
        if (m25NewPositives != null) {
            totalNewPositives += m25NewPositives.intValue();
        }
        if (m30NewPositives != null) {
            totalNewPositives += m30NewPositives.intValue();
        }
        if (m35NewPositives != null) {
            totalNewPositives += m35NewPositives.intValue();
        }
        if (m40NewPositives != null) {
            totalNewPositives += m40NewPositives.intValue();
        }
        if (m45NewPositives != null) {
            totalNewPositives += m45NewPositives.intValue();
        }
        if (m50NewPositives != null) {
            totalNewPositives += m50NewPositives.intValue();
        }
        return totalNewPositives;
    }

    public void setTotalNewPositives(Integer totalNewPositives) {this.totalNewPositives = totalNewPositives;}

    public BigInteger getFuNewNegatives() {return fuNewNegatives;}

    public void setFuNewNegatives(BigInteger fuNewNegatives) {this.fuNewNegatives = fuNewNegatives;}

    public BigInteger getF0NewNegatives() {return f0NewNegatives;}

    public void setF0NewNegatives(BigInteger f0NewNegatives) {this.f0NewNegatives = f0NewNegatives;}

    public BigInteger getF1NewNegatives() {return f1NewNegatives;}

    public void setF1NewNegatives(BigInteger f1NewNegatives) {this.f1NewNegatives = f1NewNegatives;}

    public BigInteger getF5NewNegatives() {return f5NewNegatives;}

    public void setF5NewNegatives(BigInteger f5NewNegatives) {this.f5NewNegatives = f5NewNegatives;}

    public BigInteger getF10NewNegatives() {return f10NewNegatives;}

    public void setF10NewNegatives(BigInteger f10NewNegatives) {this.f10NewNegatives = f10NewNegatives;}

    public BigInteger getF15NewNegatives() {return f15NewNegatives;}

    public void setF15NewNegatives(BigInteger f15NewNegatives) {this.f15NewNegatives = f15NewNegatives;}

    public BigInteger getF20NewNegatives() {return f20NewNegatives;}

    public void setF20NewNegatives(BigInteger f20NewNegatives) {this.f20NewNegatives = f20NewNegatives;}

    public BigInteger getF25NewNegatives() {return f25NewNegatives;}

    public void setF25NewNegatives(BigInteger f25NewNegatives) {this.f25NewNegatives = f25NewNegatives;}

    public BigInteger getF30NewNegatives() {return f30NewNegatives;}

    public void setF30NewNegatives(BigInteger f30NewNegatives) {this.f30NewNegatives = f30NewNegatives;}

    public BigInteger getF35NewNegatives() {return f35NewNegatives;}

    public void setF35NewNegatives(BigInteger f35NewNegatives) {this.f35NewNegatives = f35NewNegatives;}

    public BigInteger getF40NewNegatives() {return f40NewNegatives;}

    public void setF40NewNegatives(BigInteger f40NewNegatives) {this.f40NewNegatives = f40NewNegatives;}

    public BigInteger getF45NewNegatives() {return f45NewNegatives;}

    public void setF45NewNegatives(BigInteger f45NewNegatives) {this.f45NewNegatives = f45NewNegatives;}

    public BigInteger getF50NewNegatives() {return f50NewNegatives;}

    public void setF50NewNegatives(BigInteger f50NewNegatives) {this.f50NewNegatives = f50NewNegatives;}

    public BigInteger getMuNewNegatives() {return muNewNegatives;}

    public void setMuNewNegatives(BigInteger muNewNegatives) {this.muNewNegatives = muNewNegatives;}

    public BigInteger getM0NewNegatives() {return m0NewNegatives;}

    public void setM0NewNegatives(BigInteger m0NewNegatives) {this.m0NewNegatives = m0NewNegatives;}

    public BigInteger getM1NewNegatives() {return m1NewNegatives;}

    public void setM1NewNegatives(BigInteger m1NewNegatives) {this.m1NewNegatives = m1NewNegatives;}

    public BigInteger getM5NewNegatives() {return m5NewNegatives;}

    public void setM5NewNegatives(BigInteger m5NewNegatives) {this.m5NewNegatives = m5NewNegatives;}

    public BigInteger getM10NewNegatives() {return m10NewNegatives;}

    public void setM10NewNegatives(BigInteger m10NewNegatives) {this.m10NewNegatives = m10NewNegatives;}

    public BigInteger getM15NewNegatives() {return m15NewNegatives;}

    public void setM15NewNegatives(BigInteger m15NewNegatives) {this.m15NewNegatives = m15NewNegatives;}

    public BigInteger getM20NewNegatives() {return m20NewNegatives;}

    public void setM20NewNegatives(BigInteger m20NewNegatives) {this.m20NewNegatives = m20NewNegatives;}

    public BigInteger getM25NewNegatives() {return m25NewNegatives;}

    public void setM25NewNegatives(BigInteger m25NewNegatives) {this.m25NewNegatives = m25NewNegatives;}

    public BigInteger getM30NewNegatives() {return m30NewNegatives;}

    public void setM30NewNegatives(BigInteger m30NewNegatives) {this.m30NewNegatives = m30NewNegatives;}

    public BigInteger getM35NewNegatives() {return m35NewNegatives;}

    public void setM35NewNegatives(BigInteger m35NewNegatives) {this.m35NewNegatives = m35NewNegatives;}

    public BigInteger getM40NewNegatives() {return m40NewNegatives;}

    public void setM40NewNegatives(BigInteger m40NewNegatives) {this.m40NewNegatives = m40NewNegatives;}

    public BigInteger getM45NewNegatives() {return m45NewNegatives;}

    public void setM45NewNegatives(BigInteger m45NewNegatives) {this.m45NewNegatives = m45NewNegatives;}

    public BigInteger getM50NewNegatives() {return m50NewNegatives;}

    public void setM50NewNegatives(BigInteger m50NewNegatives) {this.m50NewNegatives = m50NewNegatives;}

    public Integer getTotalNewNegatives() {
        totalNewNegatives = 0;
        if (fuNewNegatives != null) {
            totalNewNegatives += fuNewNegatives.intValue();
        }
        if (f0NewNegatives != null) {
            totalNewNegatives += f0NewNegatives.intValue();
        }
        if (f1NewNegatives != null) {
            totalNewNegatives += f1NewNegatives.intValue();
        }
        if (f5NewNegatives != null) {
            totalNewNegatives += f5NewNegatives.intValue();
        }
        if (f10NewNegatives != null) {
            totalNewNegatives += f10NewNegatives.intValue();
        }
        if (f15NewNegatives != null) {
            totalNewNegatives += f15NewNegatives.intValue();
        }
        if (f20NewNegatives != null) {
            totalNewNegatives += f20NewNegatives.intValue();
        }
        if (f25NewNegatives != null) {
            totalNewNegatives += f25NewNegatives.intValue();
        }
        if (f30NewNegatives != null) {
            totalNewNegatives += f30NewNegatives.intValue();
        }
        if (f35NewNegatives != null) {
            totalNewNegatives += f35NewNegatives.intValue();
        }
        if (f40NewNegatives != null) {
            totalNewNegatives += f40NewNegatives.intValue();
        }
        if (f45NewNegatives != null) {
            totalNewNegatives += f45NewNegatives.intValue();
        }
        if (f50NewNegatives != null) {
            totalNewNegatives += f50NewNegatives.intValue();
        }

        if (muNewNegatives != null) {
            totalNewNegatives += muNewNegatives.intValue();
        }
        if (m0NewNegatives != null) {
            totalNewNegatives += m0NewNegatives.intValue();
        }
        if (m1NewNegatives != null) {
            totalNewNegatives += m1NewNegatives.intValue();
        }
        if (m5NewNegatives != null) {
            totalNewNegatives += m5NewNegatives.intValue();
        }
        if (m10NewNegatives != null) {
            totalNewNegatives += m10NewNegatives.intValue();
        }
        if (m15NewNegatives != null) {
            totalNewNegatives += m15NewNegatives.intValue();
        }
        if (m20NewNegatives != null) {
            totalNewNegatives += m20NewNegatives.intValue();
        }
        if (m25NewNegatives != null) {
            totalNewNegatives += m25NewNegatives.intValue();
        }
        if (m30NewNegatives != null) {
            totalNewNegatives += m30NewNegatives.intValue();
        }
        if (m35NewNegatives != null) {
            totalNewNegatives += m35NewNegatives.intValue();
        }
        if (m40NewNegatives != null) {
            totalNewNegatives += m40NewNegatives.intValue();
        }
        if (m45NewNegatives != null) {
            totalNewNegatives += m45NewNegatives.intValue();
        }
        if (m50NewNegatives != null) {
            totalNewNegatives += m50NewNegatives.intValue();
        }
        return totalNewNegatives;
    }

    public void setTotalNewNegatives(Integer totalNewNegatives) {this.totalNewNegatives = totalNewNegatives;}

    public Integer getTotalContactsTested() {
        totalContactsTested = this.getTotalKnownPositives() + this.getTotalDocumentedNegatives()
                + this.getTotalNewPositives() + this.getTotalNewNegatives();
        return totalContactsTested;
    }

    public void setTotalContactsTested(Integer totalContactsTested) {this.totalContactsTested = totalContactsTested;}
}
