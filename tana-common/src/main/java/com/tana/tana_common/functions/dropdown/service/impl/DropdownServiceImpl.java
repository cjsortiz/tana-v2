package com.tana.tana_common.functions.dropdown.service.impl;

import com.tana.tana_common.functions.dropdown.repository.DropdownRepository;
import com.tana.tana_common.functions.dropdown.service.DropdownService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.DropdownMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class DropdownServiceImpl implements DropdownService {

    @Autowired
    private DropdownRepository dropdownRepository;

    public List<DropdownMaster> fetchPlacesDropdowns() throws TanaException {
        List<DropdownMaster> list = dropdownRepository.fetchAllDropdown();
        if(ObjectUtils.isEmpty(list)){
           throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        return list;
    }
}
