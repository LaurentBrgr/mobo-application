package io.github.jhipster.application.web.rest;

import io.github.jhipster.application.MoboApp;

import io.github.jhipster.application.domain.Region;
import io.github.jhipster.application.repository.RegionRepository;
import io.github.jhipster.application.repository.search.RegionSearchRepository;
import io.github.jhipster.application.service.RegionService;
import io.github.jhipster.application.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;


import static io.github.jhipster.application.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the RegionResource REST controller.
 *
 * @see RegionResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MoboApp.class)
public class RegionResourceIntTest {

    private static final String DEFAULT_CATEGORIE_1 = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORIE_1 = "BBBBBBBBBB";

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RegionService regionService;

    /**
     * This repository is mocked in the io.github.jhipster.application.repository.search test package.
     *
     * @see io.github.jhipster.application.repository.search.RegionSearchRepositoryMockConfiguration
     */
    @Autowired
    private RegionSearchRepository mockRegionSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restRegionMockMvc;

    private Region region;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RegionResource regionResource = new RegionResource(regionService);
        this.restRegionMockMvc = MockMvcBuilders.standaloneSetup(regionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Region createEntity(EntityManager em) {
        Region region = new Region()
            .categorie1(DEFAULT_CATEGORIE_1);
        return region;
    }

    @Before
    public void initTest() {
        region = createEntity(em);
    }

    @Test
    @Transactional
    public void createRegion() throws Exception {
        int databaseSizeBeforeCreate = regionRepository.findAll().size();

        // Create the Region
        restRegionMockMvc.perform(post("/api/regions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(region)))
            .andExpect(status().isCreated());

        // Validate the Region in the database
        List<Region> regionList = regionRepository.findAll();
        assertThat(regionList).hasSize(databaseSizeBeforeCreate + 1);
        Region testRegion = regionList.get(regionList.size() - 1);
        assertThat(testRegion.getCategorie1()).isEqualTo(DEFAULT_CATEGORIE_1);

        // Validate the Region in Elasticsearch
        verify(mockRegionSearchRepository, times(1)).save(testRegion);
    }

    @Test
    @Transactional
    public void createRegionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = regionRepository.findAll().size();

        // Create the Region with an existing ID
        region.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRegionMockMvc.perform(post("/api/regions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(region)))
            .andExpect(status().isBadRequest());

        // Validate the Region in the database
        List<Region> regionList = regionRepository.findAll();
        assertThat(regionList).hasSize(databaseSizeBeforeCreate);

        // Validate the Region in Elasticsearch
        verify(mockRegionSearchRepository, times(0)).save(region);
    }

    @Test
    @Transactional
    public void getAllRegions() throws Exception {
        // Initialize the database
        regionRepository.saveAndFlush(region);

        // Get all the regionList
        restRegionMockMvc.perform(get("/api/regions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(region.getId().intValue())))
            .andExpect(jsonPath("$.[*].categorie1").value(hasItem(DEFAULT_CATEGORIE_1.toString())));
    }
    
    @Test
    @Transactional
    public void getRegion() throws Exception {
        // Initialize the database
        regionRepository.saveAndFlush(region);

        // Get the region
        restRegionMockMvc.perform(get("/api/regions/{id}", region.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(region.getId().intValue()))
            .andExpect(jsonPath("$.categorie1").value(DEFAULT_CATEGORIE_1.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingRegion() throws Exception {
        // Get the region
        restRegionMockMvc.perform(get("/api/regions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRegion() throws Exception {
        // Initialize the database
        regionService.save(region);
        // As the test used the service layer, reset the Elasticsearch mock repository
        reset(mockRegionSearchRepository);

        int databaseSizeBeforeUpdate = regionRepository.findAll().size();

        // Update the region
        Region updatedRegion = regionRepository.findById(region.getId()).get();
        // Disconnect from session so that the updates on updatedRegion are not directly saved in db
        em.detach(updatedRegion);
        updatedRegion
            .categorie1(UPDATED_CATEGORIE_1);

        restRegionMockMvc.perform(put("/api/regions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedRegion)))
            .andExpect(status().isOk());

        // Validate the Region in the database
        List<Region> regionList = regionRepository.findAll();
        assertThat(regionList).hasSize(databaseSizeBeforeUpdate);
        Region testRegion = regionList.get(regionList.size() - 1);
        assertThat(testRegion.getCategorie1()).isEqualTo(UPDATED_CATEGORIE_1);

        // Validate the Region in Elasticsearch
        verify(mockRegionSearchRepository, times(1)).save(testRegion);
    }

    @Test
    @Transactional
    public void updateNonExistingRegion() throws Exception {
        int databaseSizeBeforeUpdate = regionRepository.findAll().size();

        // Create the Region

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRegionMockMvc.perform(put("/api/regions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(region)))
            .andExpect(status().isBadRequest());

        // Validate the Region in the database
        List<Region> regionList = regionRepository.findAll();
        assertThat(regionList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Region in Elasticsearch
        verify(mockRegionSearchRepository, times(0)).save(region);
    }

    @Test
    @Transactional
    public void deleteRegion() throws Exception {
        // Initialize the database
        regionService.save(region);

        int databaseSizeBeforeDelete = regionRepository.findAll().size();

        // Delete the region
        restRegionMockMvc.perform(delete("/api/regions/{id}", region.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Region> regionList = regionRepository.findAll();
        assertThat(regionList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Region in Elasticsearch
        verify(mockRegionSearchRepository, times(1)).deleteById(region.getId());
    }

    @Test
    @Transactional
    public void searchRegion() throws Exception {
        // Initialize the database
        regionService.save(region);
        when(mockRegionSearchRepository.search(queryStringQuery("id:" + region.getId())))
            .thenReturn(Collections.singletonList(region));
        // Search the region
        restRegionMockMvc.perform(get("/api/_search/regions?query=id:" + region.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(region.getId().intValue())))
            .andExpect(jsonPath("$.[*].categorie1").value(hasItem(DEFAULT_CATEGORIE_1)));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Region.class);
        Region region1 = new Region();
        region1.setId(1L);
        Region region2 = new Region();
        region2.setId(region1.getId());
        assertThat(region1).isEqualTo(region2);
        region2.setId(2L);
        assertThat(region1).isNotEqualTo(region2);
        region1.setId(null);
        assertThat(region1).isNotEqualTo(region2);
    }
}