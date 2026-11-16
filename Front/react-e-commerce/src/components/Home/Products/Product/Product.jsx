import "./Product.css";
import headphones_pink from "@/assets/images/airpods_max_pink.jpg";
import { FaStar } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useGlobalContext } from "@/components/GlobalContext/GlobalContext";
import { toast } from "react-toastify";
import { brl } from "@/helpers/format";

const Product = ({ product }) => {
  let { store } = useGlobalContext();
  const navigate = useNavigate();
  let stars = [];
  for (let i = 0; i < product?.rating; i++) {
    stars.push(<FaStar key={i} />);
  }
  const isInCart = product?.addedToCart;

  // "Comprar agora": coloca no carrinho (se ainda não estiver) e vai para o carrinho
  const buyNow = () => {
    if (!isInCart) {
      if (store.state.cartQuantity > 10) {
        toast.warning("You can only add 10 items to cart");
        return;
      }
      store.addToCart(product?._id);
    }
    navigate("/cart");
  };

  return (
    <div className="product-container">
      <div className="image">
        <img
          src={product?.product_image || headphones_pink}
          alt="Product Image"
          width={"100%"}
        />
      </div>
      <div className="product-details">
        <div className="name-add-to-cart"></div>
        <div className="price">
          <div className="name-price-product">
            <h4>{product?.name}</h4>
            <h5>
              <span className="actual-product-price">{brl(product?.price)}</span>
            </h5>
          </div>
          <h5>{product?.description}</h5>
          <div className="star-rating">
            <div className="star">{stars}</div>
            <span>({parseInt(Math.random() * 100)} Reviews)</span>
          </div>
        </div>
        <div>
          {isInCart == false ? (
            <button
              className="add-to-cart"
              onClick={() => {
                if (store.state.cartQuantity > 10) {
                  toast.warning("You can only add 10 items to cart");
                  return;
                }
                store.addToCart(product?._id);
              }}
            >
              Add to Cart
            </button>
          ) : (
            <button
              className="add-to-cart"
              onClick={() => {
                store.removeFromCart(product?._id);
              }}
            >
              Remove from cart
            </button>
          )}
          <button
            className="add-to-cart"
            style={{ marginTop: 8 }}
            onClick={buyNow}
          >
            Comprar agora
          </button>
        </div>
      </div>
      <div className="heart"></div>
    </div>
  );
};
export default Product;
