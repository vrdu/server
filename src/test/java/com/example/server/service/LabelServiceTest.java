package com.example.server.service;


import com.example.server.entity.LabelFamily;
import com.example.server.repository.LabelFamilyRepository;
import com.example.server.repository.LabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private LabelFamilyRepository labelFamilyRepository;

    LabelFamily newLabelFamily;

    @InjectMocks
    private LabelService labelService;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.reset(labelRepository, labelFamilyRepository);
        newLabelFamily = new LabelFamily();
        newLabelFamily.setId(1L);
        newLabelFamily.setLabelFamilyName("TestFamily");
        newLabelFamily.setProjectName("TestProject");
        newLabelFamily.setOwner("TestOwner");
        newLabelFamily.setLabelFamilyDescription(" ");
        newLabelFamily.setRegister(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        Mockito.reset(labelRepository, labelFamilyRepository);
    }


    @Test
    void testUpdateLabelFamily_NewLabelFamily() {
        newLabelFamily.setOldLabelFamilyName(null);
        when(labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(
                newLabelFamily.getOwner(),
                newLabelFamily.getProjectName(),
                newLabelFamily.getLabelFamilyName()
        )).thenReturn(Optional.of(newLabelFamily));
        newLabelFamily.setLabelFamilyName("NewTestFamilyName");
        labelService.updateLabelFamily(newLabelFamily);

        verify(labelFamilyRepository, times(1)).save(newLabelFamily);
        verify(labelFamilyRepository).findByOwnerAndProjectNameAndLabelFamilyName(
                newLabelFamily.getOwner(),
                newLabelFamily.getProjectName(),
                "NewTestFamilyName");
    }
    @Test
    void testUpdateLabelFamily_NewLabelFamilyNewLabelFamilyNameIsNull() {
        newLabelFamily.setOldLabelFamilyName(null);
        when(labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(
                newLabelFamily.getOwner(),
                newLabelFamily.getProjectName(),
                newLabelFamily.getLabelFamilyName()
        )).thenReturn(Optional.of(newLabelFamily));
        newLabelFamily.setLabelFamilyName(null);
        labelService.updateLabelFamily(newLabelFamily);

        verify(labelFamilyRepository, times(1)).save(newLabelFamily);
        verify(labelFamilyRepository).findByOwnerAndProjectNameAndLabelFamilyName(
                newLabelFamily.getOwner(),
                newLabelFamily.getProjectName(),
                " ");
    }


    @Test
    void testUpdateLabelFamily_ExistingFamilyConflict() {
        // Mock existing family
        LabelFamily existingFamily = new LabelFamily();
        existingFamily.setLabelFamilyName("ExistingFamily");
        existingFamily.setProjectName("TestProject");
        existingFamily.setOwner("TestOwner");

        LabelFamily newLabelFamily = new LabelFamily();
        newLabelFamily.setLabelFamilyName("ExistingFamily");
        newLabelFamily.setProjectName("TestProject");
        newLabelFamily.setOwner("TestOwner");
        newLabelFamily.setRegister(true);

        when(labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(
                newLabelFamily.getOwner(),
                newLabelFamily.getProjectName(),
                "ExistingFamily"))
                .thenReturn(Optional.of(existingFamily));

        // Call method under test and assert exception
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            labelService.updateLabelFamily(newLabelFamily);
        });

        assertEquals("LabelFamily with name 'ExistingFamily' already exists.", exception.getReason());
    }

    @Test
    void testGetLabelFamilies() {
        List<LabelFamily> expectedLabelFamilies = new ArrayList<>();
        // Arrange
        LabelFamily inputLabelFamily = new LabelFamily();
        inputLabelFamily.setProjectName("TestProject");
        inputLabelFamily.setOwner("TestOwner");

        LabelFamily labelFamily1 = new LabelFamily();
        labelFamily1.setId(1L);
        labelFamily1.setProjectName("TestProject");
        labelFamily1.setOwner("TestOwner");
        expectedLabelFamilies.add(labelFamily1);

        LabelFamily labelFamily2 = new LabelFamily();
        labelFamily2.setId(2L);
        labelFamily2.setProjectName("TestProject");
        labelFamily2.setOwner("TestOwner");
        expectedLabelFamilies.add(labelFamily2);


        when(labelFamilyRepository.findAllByProjectNameAndOwner("TestProject", "TestOwner"))
                .thenReturn(expectedLabelFamilies);

        // Act
        List<LabelFamily> actualLabelFamilies = labelService.getLabelFamilies(inputLabelFamily);
        System.out.println("acutalLabelFamilies: "+actualLabelFamilies.toString());
        // Assert
        assertNotNull(actualLabelFamilies);
        assertEquals(2, actualLabelFamilies.size());
        assertEquals("TestProject", actualLabelFamilies.get(0).getProjectName());
        assertEquals("TestOwner", actualLabelFamilies.get(1).getOwner());

        verify(labelFamilyRepository, times(1))
                .findAllByProjectNameAndOwner("TestProject", "TestOwner");
    }

}
