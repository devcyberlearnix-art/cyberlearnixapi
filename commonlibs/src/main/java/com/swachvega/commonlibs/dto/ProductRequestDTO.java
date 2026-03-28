package com.cyberlearnix.commonlibs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductRequestDTO {

    private String gtin;
    private String sku;
    private String productName;
    private String brandName;
    private String manufacturerName;
    private String category;
    @JsonAlias({"sub_category"})
    private String subCategory;
    
    // Category relationship fields
    @JsonAlias({"category_id", "categoryId", "primary_category_id", "primaryCategoryId"})
    private Long primaryCategoryId;      // Primary category ID
    private List<Long> categoryIds;      // Additional category IDs for multi-categorization
    
    // SubCategory relationship fields
    @JsonAlias({"sub_category_id"})
    private Long subCategoryId;          // Primary subcategory ID
    private List<Long> subCategoryIds;   // Additional subcategory IDs for multi-subcategorization

    private Long subSubCategoryId;
    
    private String description;
    @JsonAlias({"short_description"})
    private String shortDescription;
    private BigDecimal mrp;
    private String currency;
    private String unitOfMeasure;
    private BigDecimal netContent;
    private String netContentUom;
    @JsonAlias({"packaging_type"})
    private String packagingType;
    @JsonAlias({"measurement_unit"})
    private String measurementUnit;
    @JsonAlias({"dimension_unit"})
    private String dimensionUnit;
    @JsonAlias({"dimension_width"})
    private BigDecimal dimensionWidth;
    @JsonAlias({"dimension_height"})
    private BigDecimal dimensionHeight;
    @JsonAlias({"dimension_depth"})
    private BigDecimal dimensionDepth;
    private String dimensionsLwhCm;
    private BigDecimal grossWeightG;
    @JsonAlias({"gross_weight"})
    private String grossWeight;
    @JsonAlias({"net_weight"})
    private String netWeight;
    @JsonAlias({"country_of_origin"})
    private String countryOfOrigin;
    private String hsnCode;
    @JsonAlias({"hs_code"})
    private String hsCode;
    private BigDecimal gstRate;
    private Boolean taxInclusive;
    private BigDecimal igstRate;
    private BigDecimal cgstRate;
    private BigDecimal sgstRate;
    private BigDecimal igst;
    private BigDecimal cgst;
    private BigDecimal sgst;
    @JsonAlias({"shelf_life_value"})
    private Integer shelfLifeValue;
    @JsonAlias({"shelf_life_unit"})
    private String shelfLifeUnit;
    @JsonAlias({"shelf_life_based_on"})
    private String shelfLifeBasedOn;
    @JsonAlias({"storage_condition"})
    private String storageCondition;
    @JsonAlias({"caution_warning"})
    private String cautionWarning;
    @JsonAlias({"activation_date"})
    private LocalDate activationDate;
    @JsonAlias({"deactivation_date"})
    private LocalDate deactivationDate;
    @JsonAlias({"fssai_license_no"})
    private String fssaiLicenseNo;
    private List<String> imageUrls;
    private String barcodeImageUrl;
    @JsonAlias({"front_image_url"})
    private String frontImageUrl;
    @JsonAlias({"back_image_url"})
    private String backImageUrl;
    @JsonAlias({"top_image_url"})
    private String topImageUrl;
    @JsonAlias({"bottom_image_url"})
    private String bottomImageUrl;
    @JsonAlias({"left_image_url"})
    private String leftImageUrl;
    @JsonAlias({"right_image_url"})
    private String rightImageUrl;
    @JsonAlias({"artwork_front_image_url"})
    private String artworkFrontImageUrl;
    @JsonAlias({"artwork_back_image_url"})
    private String artworkBackImageUrl;
    private Map<String, String> nutritionalInfo;
    private String ingredients;
    private String allergens;
    private String vegetarianNonveg;
    private String storageInstructions;
    private String manufactureDate;
    private String expiryDate;
    private String batchNumber;
    private String primaryGtin;
    private String url;
    @JsonAlias({"marketing_info"})
    private String marketingInfo;
    @JsonAlias({"derived_description"})
    private String derivedDescription;
    private String brand;
    private String name;
    @JsonAlias({"company_name"})
    private String companyName;

    // Company address fields (DataKart compatibility)
    @JsonAlias({"company_address1"})
    private String companyAddress1;
    
    @JsonAlias({"company_address2"})
    private String companyAddress2;
    
    @JsonAlias({"company_city"})
    private String companyCity;
    
    @JsonAlias({"company_state"})
    private String companyState;
    
    @JsonAlias({"company_pincode"})
    private String companyPincode;
    
    @JsonAlias({"company_country"})
    private String companyCountry;

    // Age group (DataKart compatibility)
    @JsonAlias({"age_group"})
    private String ageGroup;

    // MRP target market fields (DataKart compatibility)
    @JsonAlias({"mrp_target_market"})
    private String mrpTargetMarket;
    
    @JsonAlias({"mrp_location"})
    private String mrpLocation;
    
    @JsonAlias({"mrp_activation_date"})
    private LocalDate mrpActivationDate;

    @JsonAlias({"case_configuration"})
    private Object caseConfiguration;  // Can be array or map
    private Integer rank;
    private String gtinStatus;

    // New fields to support ranking & inventory
    private Boolean premiumStatus;

    // (You can also add source/createdByStoreId/isCustom here if you need them in your create payload)

    @JsonSetter("gtin")
    public void setGtin(Object gtin) {
        this.gtin = gtin == null ? null : String.valueOf(gtin);
    }

    @JsonSetter("brandName")
    public void setBrandName(String brandName) {
        this.brandName = brandName;
        if ((this.brand == null || this.brand.isBlank()) && brandName != null && !brandName.isBlank()) {
            this.brand = brandName;
        }
    }

    @JsonSetter("brand")
    public void setBrand(String brand) {
        this.brand = brand;
        if ((this.brandName == null || this.brandName.isBlank()) && brand != null && !brand.isBlank()) {
            this.brandName = brand;
        }
    }

    @JsonSetter("productName")
    public void setProductName(String productName) {
        this.productName = productName;
        if ((this.name == null || this.name.isBlank()) && productName != null && !productName.isBlank()) {
            this.name = productName;
        }
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
        if ((this.productName == null || this.productName.isBlank()) && name != null && !name.isBlank()) {
            this.productName = name;
        }
    }

    private static BigDecimal tryParseBigDecimal(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    @JsonSetter("igstRate")
    public void setIgstRate(BigDecimal igstRate) {
        this.igstRate = igstRate;
        if (this.igst == null && igstRate != null) {
            this.igst = igstRate;
        }
    }

    @JsonSetter("cgstRate")
    public void setCgstRate(BigDecimal cgstRate) {
        this.cgstRate = cgstRate;
        if (this.cgst == null && cgstRate != null) {
            this.cgst = cgstRate;
        }
    }

    @JsonSetter("sgstRate")
    public void setSgstRate(BigDecimal sgstRate) {
        this.sgstRate = sgstRate;
        if (this.sgst == null && sgstRate != null) {
            this.sgst = sgstRate;
        }
    }

    @JsonSetter("igst")
    public void setIgst(BigDecimal igst) {
        this.igst = igst;
        if (this.igstRate == null && igst != null) {
            this.igstRate = igst;
        }
    }

    @JsonSetter("cgst")
    public void setCgst(BigDecimal cgst) {
        this.cgst = cgst;
        if (this.cgstRate == null && cgst != null) {
            this.cgstRate = cgst;
        }
    }

    @JsonSetter("sgst")
    public void setSgst(BigDecimal sgst) {
        this.sgst = sgst;
        if (this.sgstRate == null && sgst != null) {
            this.sgstRate = sgst;
        }
    }

    @JsonSetter("id")
    public void setId(Object id) {
        this.gtin = id == null ? null : String.valueOf(id);
    }

    // ===== DataKart Nested Structure Support =====
    
    // Inner classes for DataKart nested objects
    @Getter
    @Setter
    public static class CompanyDetail {
        private String name;
        private Address address;
    }
    
    @Getter
    @Setter
    public static class Address {
        private String address1;
        private String address2;
        private String city;
        private String state;
        private String country;
        private String pincode;
    }
    
    @Getter
    @Setter
    public static class WeightsAndMeasures {
        @JsonAlias({"measurement_unit"})
        private String measurementUnit;
        @JsonAlias({"net_weight"})
        private String netWeight;
        @JsonAlias({"gross_weight"})
        private String grossWeight;
        @JsonAlias({"net_content"})
        private String netContent;
    }
    
    @Getter
    @Setter
    public static class Dimensions {
        @JsonAlias({"measurement_unit"})
        private String measurementUnit;
        private BigDecimal height;
        private BigDecimal width;
        private BigDecimal depth;
    }
    
    @Getter
    @Setter
    public static class Images {
        private String front;
        private String back;
        private String top;
        private String bottom;
        private String left;
        private String right;
        @JsonAlias({"artwork_front"})
        private String artworkFront;
        @JsonAlias({"artwork_back"})
        private String artworkBack;
        private String barcode;
    }
    
    @Getter
    @Setter
    public static class MrpItem {
        private BigDecimal mrp;
        @JsonAlias({"target_market"})
        private String targetMarket;
        private String currency;
        private String location;
        @JsonAlias({"activation_date"})
        private String activationDate;
    }
    
    @Getter
    @Setter
    public static class Attributes {
        @JsonAlias({"shelf_life"})
        private ShelfLife shelfLife;
        @JsonAlias({"age_group"})
        private String ageGroup;
    }
    
    @Getter
    @Setter
    public static class ShelfLife {
        private ShelfLifeChild child;
    }
    
    @Getter
    @Setter
    public static class ShelfLifeChild {
        private String value;
        private String unit;
        @JsonAlias({"based_on"})
        private String basedOn;
    }
    
    // Custom setters to handle DataKart nested structure
    @JsonSetter("company_detail")
    public void setCompanyDetail(CompanyDetail companyDetail) {
        if (companyDetail != null) {
            this.companyName = companyDetail.getName();
            if (companyDetail.getAddress() != null) {
                Address addr = companyDetail.getAddress();
                this.companyAddress1 = addr.getAddress1();
                this.companyAddress2 = addr.getAddress2();
                this.companyCity = addr.getCity();
                this.companyState = addr.getState();
                this.companyCountry = addr.getCountry();
                this.companyPincode = addr.getPincode();
            }
        }
    }
    
    @JsonSetter("weights_and_measures")
    public void setWeightsAndMeasures(WeightsAndMeasures wam) {
        if (wam != null) {
            this.measurementUnit = wam.getMeasurementUnit();
            this.netWeight = wam.getNetWeight();
            this.grossWeight = wam.getGrossWeight();
            
            // Parse net_content (e.g., "1 each", "32 g")
            if (wam.getNetContent() != null) {
                String netContentStr = wam.getNetContent().trim();
                String[] parts = netContentStr.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        this.netContent = new BigDecimal(parts[0]);
                        this.netContentUom = parts[1];
                    } catch (Exception e) {
                        // If parsing fails, store as is
                        this.netContentUom = netContentStr;
                    }
                }
            }
            
            // Convert weights to grams if in kg
            if (wam.getGrossWeight() != null && "kg".equalsIgnoreCase(wam.getMeasurementUnit())) {
                try {
                    BigDecimal weightKg = new BigDecimal(wam.getGrossWeight());
                    this.grossWeightG = weightKg.multiply(new BigDecimal("1000"));
                } catch (Exception e) {
                    // Ignore conversion errors
                }
            }
        }
    }
    
    @JsonSetter("dimensions")
    public void setDimensions(Dimensions dims) {
        if (dims != null) {
            this.dimensionUnit = dims.getMeasurementUnit();
            this.dimensionHeight = dims.getHeight();
            this.dimensionWidth = dims.getWidth();
            this.dimensionDepth = dims.getDepth();
            
            // Build dimensionsLwhCm string
            if (dims.getWidth() != null && dims.getDepth() != null && dims.getHeight() != null) {
                this.dimensionsLwhCm = String.format("%sx%sx%s",
                    dims.getWidth().stripTrailingZeros().toPlainString(),
                    dims.getDepth().stripTrailingZeros().toPlainString(),
                    dims.getHeight().stripTrailingZeros().toPlainString()
                );
            }
        }
    }
    
    @JsonSetter("images")
    public void setImages(Images images) {
        if (images != null) {
            this.frontImageUrl = images.getFront();
            this.backImageUrl = images.getBack();
            this.topImageUrl = images.getTop();
            this.bottomImageUrl = images.getBottom();
            this.leftImageUrl = images.getLeft();
            this.rightImageUrl = images.getRight();
            this.artworkFrontImageUrl = images.getArtworkFront();
            this.artworkBackImageUrl = images.getArtworkBack();
            this.barcodeImageUrl = images.getBarcode();
        }
    }
    
    @JsonSetter("mrp")
    public void setMrpArray(Object mrpObj) {
        // Handle both BigDecimal (flat) and List (DataKart nested array)
        if (mrpObj instanceof List) {
            List<?> mrpList = (List<?>) mrpObj;
            if (!mrpList.isEmpty() && mrpList.get(0) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mrpMap = (Map<String, Object>) mrpList.get(0);
                
                Object mrpValue = mrpMap.get("mrp");
                if (mrpValue != null) {
                    this.mrp = new BigDecimal(String.valueOf(mrpValue));
                }
                
                Object currency = mrpMap.get("currency");
                if (currency != null) {
                    this.currency = String.valueOf(currency);
                }
                
                Object targetMarket = mrpMap.get("target_market");
                if (targetMarket != null) {
                    this.mrpTargetMarket = String.valueOf(targetMarket);
                }
                
                Object location = mrpMap.get("location");
                if (location != null) {
                    this.mrpLocation = String.valueOf(location);
                }
                
                Object activationDate = mrpMap.get("activation_date");
                if (activationDate != null) {
                    try {
                        this.mrpActivationDate = LocalDate.parse(String.valueOf(activationDate));
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                }
            }
        } else if (mrpObj instanceof Number) {
            this.mrp = new BigDecimal(String.valueOf(mrpObj));
        }
    }
    
    @JsonSetter("attributes")
    public void setAttributes(Attributes attrs) {
        if (attrs != null) {
            this.ageGroup = attrs.getAgeGroup();
            
            if (attrs.getShelfLife() != null && attrs.getShelfLife().getChild() != null) {
                ShelfLifeChild sl = attrs.getShelfLife().getChild();
                try {
                    this.shelfLifeValue = Integer.parseInt(sl.getValue());
                } catch (Exception e) {
                    // Ignore parse errors
                }
                this.shelfLifeUnit = sl.getUnit();
                this.shelfLifeBasedOn = sl.getBasedOn();
            }
        }
    }
    
    @JsonSetter("hs_code")
    public void setHsCodeFromSnakeCase(String hsCode) {
        this.hsCode = hsCode;
        if (this.hsnCode == null || this.hsnCode.isBlank()) {
            this.hsnCode = hsCode;
        }
    }
}
