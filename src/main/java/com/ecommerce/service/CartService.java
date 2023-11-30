package com.ecommerce.service;

import com.ecommerce.configuration.JwtRequestFilter;
import com.ecommerce.dao.CartDao;
import com.ecommerce.dao.ProductDao;
import com.ecommerce.dao.CartItemDao;
import com.ecommerce.dao.UserDao;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartDao cartDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CartItemDao cartItemDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);

    public Cart addToCart(Integer productId, Integer quantity) {
        LOGGER.info("Adding item to cart");
        Product product = productDao.findById(productId).get();
        String currentUser = JwtRequestFilter.CURRENT_USER;

        if (currentUser != null) {
            User user = userDao.findById(currentUser).get();
            LOGGER.info("Logged in user: "+ user.getUserName());
            Cart userCart = cartDao.findByUser(user);
            LOGGER.info("User cart: "+ userCart);

            if(userCart != null){
                List<CartItem> cartItems = userCart.getCartItems().stream().
                        filter(pi -> pi.getProduct().getProductId().equals(productId)).collect(Collectors.toList());
                //the cart does not contain the product
                if(cartItems.size() == 0){
                    CartItem newItem = new CartItem(product, 1);
                    newItem.setCart(userCart);
                    cartItemDao.save(newItem);
                    userCart.getCartItems().add(newItem);
                } else{
                    cartItems.get(0).setQuantity(quantity);
                }
                return cartDao.save(userCart);
            }else {
                //create new cart
                Cart newCart = new Cart();
                //add the product
                CartItem newItem = new CartItem(product, 1);
                newItem.setCart(newCart);
                List<CartItem> cartItems = new ArrayList<>();
                cartItems.add(newItem);
                newCart.setCartItems(cartItems);
                newCart.setUser(user);
                //return  the newly created cart
                return cartDao.save(newCart);
            }
        }
       return null;
    }

    public Cart getCart(){
        String currentUser = JwtRequestFilter.CURRENT_USER;
        User user = userDao.findById(currentUser).get();
        return cartDao.findByUser(user);
    }
    @Transactional
    public void deleteCartItem(Integer cartId, Integer productId){
        LOGGER.info("Removing item from cart");
        Cart cart = cartDao.findById(cartId).get();
        cartItemDao.deleteByCartAndAndProduct_ProductId(cart, productId);
    }
}
