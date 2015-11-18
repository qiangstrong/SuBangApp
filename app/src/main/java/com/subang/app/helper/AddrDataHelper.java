package com.subang.app.helper;

import com.subang.bean.AddrData;
import com.subang.bean.AddrDetail;
import com.subang.bean.OrderDetail;
import com.subang.domain.City;
import com.subang.domain.District;
import com.subang.domain.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2015/11/12.
 */
public class AddrDataHelper {

    public static String getAreaDes(OrderDetail orderDetail) {
        StringBuilder builder = new StringBuilder();
        builder.append(orderDetail.getCityname());
        builder.append("  ");
        builder.append(orderDetail.getDistrictname());
        builder.append("  ");
        builder.append(orderDetail.getRegionname());
        return builder.toString();
    }

    public static String getAreaDes(AddrDetail addrDetail) {
        StringBuilder builder = new StringBuilder();
        builder.append(addrDetail.getCityname());
        builder.append("  ");
        builder.append(addrDetail.getDistrictname());
        builder.append("  ");
        builder.append(addrDetail.getRegionname());
        return builder.toString();
    }

    //添加地址界面生成的关于area的描述
    public static String getAreaDes(AddrData addrData) {
        StringBuilder builder = new StringBuilder();
        builder.append(addrData.getDefaultCityname());
        builder.append("  ");
        builder.append(addrData.getDefaultDistrictname());
        builder.append("  ");
        builder.append(addrData.getDefaultRegionname());
        return builder.toString();
    }
    
    //不复制list和detail
    public static void copy(AddrData from ,AddrData to){
        to.setDefaultCityid(from.getDefaultCityid());
        to.setDefaultCityname(from.getDefaultCityname());
        to.setDefaultDistrictid(from.getDefaultDistrictid());
        to.setDefaultDistrictname(from.getDefaultDistrictname());
        to.setDefaultRegionid(from.getDefaultRegionid());
        to.setDefaultRegionname(from.getDefaultRegionname());
    }
    
    public static List<String> toCityList(AddrData addrData){
        List<String> citynames=new ArrayList<>();
        List<City> citys=addrData.getCitys();
        for (City city:citys) {
            citynames.add(city.getName());
        }
        return  citynames;
    }

    public static List<String> toDistrictList(AddrData addrData){
        List<String> districtnames=new ArrayList<>();
        List<District> districts=addrData.getDistricts();
        for (District district:districts) {
            districtnames.add(district.getName());
        }
        return  districtnames;
    }

    public static List<String> toRegionList(AddrData addrData){
        List<String> regionnames=new ArrayList<>();
        List<Region> regions=addrData.getRegions();
        for (Region region:regions) {
            regionnames.add(region.getName());
        }
        return  regionnames;
    }
}
