package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.SupplierRequest;
import com.agriculture.warehouse.entity.Supplier;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "供应商不存在"));
    }

    public List<Supplier> searchSuppliers(String name) {
        if (name == null || name.isEmpty()) {
            return getAllSuppliers();
        }
        return supplierRepository.findByNameContaining(name);
    }

    public List<Supplier> getSuppliersByCategory(String category) {
        return supplierRepository.findByCategory(category);
    }

    public Supplier createSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setCategory(request.getCategory());
        supplier.setRemark(request.getRemark());
        supplier.setCreateTime(LocalDateTime.now());
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = getSupplierById(id);
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setCategory(request.getCategory());
        supplier.setRemark(request.getRemark());
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplierRepository.delete(supplier);
    }
}