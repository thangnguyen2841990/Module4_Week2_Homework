package com.codegym.controller;

import com.codegym.model.Category;
import com.codegym.model.Product;
import com.codegym.model.ProductForm;
import com.codegym.service.category.ICategoryService;
import com.codegym.service.product.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private IProductService productService;
    @Autowired
    private ICategoryService categoryService;
    @Value("C:/Users/nguye/OneDrive/Desktop/image/")
    private String fileUpload;

    @ModelAttribute(name = "categories")
    private Page<Category> categories(Pageable pageable) {
        return categoryService.findAll(pageable);
    }


    @GetMapping
    private ModelAndView showAllProduct(String name,Optional<Integer> page) {
       Pageable pageable = PageRequest.of(page.orElse(0),10);
        if (name == null ) {
            Page<Product> products = this.productService.findAll(pageable);
            ModelAndView modelAndView = new ModelAndView("/product/list");
            modelAndView.addObject("products", products);
            return modelAndView;
        } else {
            Page<Product> products = this.productService.findByNameContaining(name, pageable);
            ModelAndView modelAndView = new ModelAndView("/product/list");
            modelAndView.addObject("products", products);
            modelAndView.addObject("name", name);

            return modelAndView;
        }
    }

    @GetMapping("/create")
    private ModelAndView showCreateForm() {
        ProductForm productForm = new ProductForm();
        ModelAndView modelAndView = new ModelAndView("/product/create");
        modelAndView.addObject("productForm", productForm);
        return modelAndView;
    }

    @PostMapping("/create")
    private ModelAndView createProduct(@ModelAttribute ProductForm productForm) {
        MultipartFile imageFile = productForm.getImage();
        String fileName = imageFile.getOriginalFilename();
        long currentTime = System.currentTimeMillis();
        fileName = currentTime + fileName;
        try {
            FileCopyUtils.copy(imageFile.getBytes(), new File(fileUpload + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Product newProduct = new Product(productForm.getName(), productForm.getPrice(), productForm.getQuantity(), productForm.getDescription(), fileName, productForm.getCategory());
        this.productService.save(newProduct);
        ModelAndView modelAndView = new ModelAndView("redirect:/products");
        return modelAndView;
    }

    @GetMapping("/edit/{id}")
    private ModelAndView showEditForm(@PathVariable Long id) {
        Optional<Product> product = this.productService.findById(id);
        if (!product.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("/product/error-404");
            return modelAndView;
        }
        ModelAndView modelAndView = new ModelAndView("/product/edit");
        modelAndView.addObject("product",product.get());
        return modelAndView;
    }
    @PostMapping("/edit/{id}")
    private ModelAndView editProduct(@PathVariable Long id, @ModelAttribute ProductForm productForm){
        Optional<Product> product = this.productService.findById(id);
        if (!product.isPresent()){
            ModelAndView modelAndView = new ModelAndView("/product/error-404");
            return modelAndView;
        }
        Product oldProduct = product.get();
        MultipartFile imageFile = productForm.getImage();
        String image;
        if (imageFile.getSize() == 0){
            image = oldProduct.getImage();
        }else {
            String fileName = imageFile.getOriginalFilename();
            long currentTime = System.currentTimeMillis();
            fileName = currentTime + fileName;
            image = fileName;
            try {
                FileCopyUtils.copy(imageFile.getBytes(), new File(fileUpload + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
         Product newProduct = new Product(productForm.getId(),productForm.getName(),productForm.getPrice()
         , productForm.getQuantity(), productForm.getDescription(), image,productForm.getCategory());
        this.productService.save(newProduct);
        ModelAndView modelAndView = new ModelAndView("redirect:/products");
        return modelAndView;
    }
    @GetMapping("/delete/{id}")
    private ModelAndView showDeleteForm(@PathVariable Long id){
        Optional<Product> product = this.productService.findById(id);
        if (!product.isPresent()){
            ModelAndView modelAndView = new ModelAndView("/product/error-404");
            return modelAndView;
        }
        ModelAndView modelAndView = new ModelAndView("/product/delete");
        modelAndView.addObject("product",product.get());
        return modelAndView;
    }
    @PostMapping("/delete/{id}")
    private ModelAndView deleteProduct(@PathVariable Long id){
        Optional<Product> product = this.productService.findById(id);
        Product oldProduct = product.get();
        if (!product.isPresent()){
            ModelAndView modelAndView = new ModelAndView("/product/error-404");
            return modelAndView;
        }
        File file = new File(fileUpload + oldProduct.getImage());
        if (file.exists()){
            file.delete();
        }
        this.productService.deleteById(id);
        ModelAndView modelAndView = new ModelAndView("redirect:/products");
        return modelAndView;
    }
    @GetMapping("/view/{id}")
    private ModelAndView showProductDetails(@PathVariable Long id){
        Optional<Product> product = this.productService.findById(id);
        if (!product.isPresent()){
            ModelAndView modelAndView = new ModelAndView("/product/error-404");
            return modelAndView;
        }
        ModelAndView modelAndView = new ModelAndView("/product/view");
        modelAndView.addObject("product",product.get());
        return modelAndView;
    }

    @GetMapping("/search")
    private ModelAndView showAllProductByCategory(Long categoryId) {
        if (categoryId != null) {
            Page<Product> products = this.productService.findByCategory(categoryId);
            ModelAndView modelAndView = new ModelAndView("/product/list");
            modelAndView.addObject("products", products);
            return modelAndView;
        }
        return null;
    }
}
