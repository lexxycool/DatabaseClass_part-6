package com.techelevator.dao;

import com.techelevator.model.City;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class JdbcCityDaoTests extends UnitedStatesDaoTests {

    private City testCity;
    private JdbcCityDao sut;

    @Before
    public void setUp() {
        sut = new JdbcCityDao(dataSource);
        testCity = new City(0, "Test City", "CC", 99, 999);
        System.out.println("Child's @Before");
    }

    @Test
    public void getCity_returns_correct_city_for_id() {
        City city1 = new City(1, "City 1", "AA", 11, 111);

        City cityActual = sut.getCity(1);

        assertCitiesMatch(city1, cityActual);

    }

    @Test
    public void getCitiesByState_return_all_cities_for_state() {
        City city1 = new City(1, "City 1", "AA", 11, 111);
        City city4 = new City(4, "City 4", "AA", 44, 444);

        List<City> cities = sut.getCitiesByState("AA");

        Assert.assertEquals(2, cities.size());

        boolean city1Found = false;
        boolean city4Found = false;

        for(City city: cities) {
            if(city.getCityName().equals("City 1")) {
                city1Found = true;
            }
            if(city.getCityName().equals("City 4")) {
                city4Found = true;
            }
        }
        boolean bothFound = city1Found && city4Found;

        Assert.assertTrue(bothFound);

    }

    @Test
    public void getCitiesByState_returns_empty_list_for_abbreviation_not_in_db() {

        List<City> cities = sut.getCitiesByState("XX");

        Assert.assertEquals(0, cities.size());


    }

    @Test
    public void created_city_has_expected_values_when_retrieved() {

        City createdCity = sut.createCity(testCity);

        long newId = createdCity.getCityId();

        City retrievedCity = sut.getCity(newId);

        assertCitiesMatch(createdCity, retrievedCity);

    }

    @Test
    public void updates_city_has_expected_values_when_retrieved() {
        City cityUpdate = sut.getCity(1);

        cityUpdate.setArea(999);
        cityUpdate.setPopulation(9876);

        sut.updateCity(cityUpdate);

        City retrievedCity = sut.getCity(1);

        assertCitiesMatch(cityUpdate, retrievedCity);

    }

    @Test
    public void deleted_city_cant_be_retrieved() {
        List<City> listBeforeDeletion = sut.getCitiesByState("AA");
        int sizeBeforeDeletion = listBeforeDeletion.size();

        sut.deleteCity(4);

        List<City> listAfterDeletion = sut.getCitiesByState("AA");
        int sizeAfterDeletion = listAfterDeletion.size();

        Assert.assertEquals(sizeBeforeDeletion - 1, sizeAfterDeletion);


    }

    private void assertCitiesMatch(City expected, City actual) {
        Assert.assertEquals(expected.getCityId(), actual.getCityId());
        Assert.assertEquals(expected.getCityName(), actual.getCityName());
        Assert.assertEquals(expected.getStateAbbreviation(), actual.getStateAbbreviation());
        Assert.assertEquals(expected.getPopulation(), actual.getPopulation());
        Assert.assertEquals(expected.getArea(), actual.getArea(), 0.1);
    }



}
