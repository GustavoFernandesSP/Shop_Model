import "./OrderSummary.css";
import { useGlobalContext } from "@/components/GlobalContext/GlobalContext";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { brl } from "@/helpers/format";

const OrderSummary = () => {
  const { store, modal, auth } = useGlobalContext();
  const navigate = useNavigate();
  const [deliveryType, setDeliveryType] = useState("Standard");
  const [phone, setPhone] = useState("");
  const [processing, setProcessing] = useState(false);

  // TODO Fase 2: frete calculado pelo CEP no servidor (hoje valor fixo)
  const deliveryCost = deliveryType == "Standard" ? 5 : 10;
  const hasItems = store.state.cartQuantity > 0;

  const checkOut = async () => {
    // 1) precisa estar logado: se não, abre o modal de login
    if (!auth.state.user) {
      toast.info("Faça login ou cadastre-se para finalizar a compra");
      modal.openModal(false);
      return;
    }
    // 2) telefone obrigatório
    if (phone.trim().length === 0) {
      toast.error("Informe o telefone para continuar");
      return;
    }

    setProcessing(true);
    try {
      const response = await store.confirmOrder({
        DeliveryType: deliveryType,
        DeliveryTypeCost: deliveryCost,
        costAfterDelieveryRate: store.state.cartTotal + deliveryCost,
        promoCode: "",
        phoneNumber: phone,
        user_id: auth.state.user?.id,
      });
      if (response?.showRegisterLogin) {
        modal.openModal(false);
        return;
      }
      // 3) pedido criado: vai para o acompanhamento
      if (response === true) navigate("/delivery");
    } catch (e) {
      toast.error("Não foi possível finalizar o pedido. Tente novamente.");
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="is-order-summary">
      <div className="sub-container">
        <div className="contains-order">
          <div className="total-cost">
            <h4>Total Items ({store.state.cartQuantity})</h4>
            <h4>{brl(store.state.cartTotal)}</h4>
          </div>
          <div className="shipping">
            <h4>Shipping</h4>
            <select
              className="select-dropdown"
              value={deliveryType}
              onChange={(item) => {
                setDeliveryType(item.target.value);
              }}
            >
              <option value="Standard" className="select">
                Standard
              </option>
              <option value="Express" className="select">
                Express
              </option>
            </select>
          </div>
          <div className="promo-code">
            <h4>Promo Code</h4>
            <div className="enter-promo">
              <input className="select-dropdown" type="text" />
              <button className="flat-button apply-promo" disabled={!hasItems}>
                Apply
              </button>
            </div>
          </div>
          <div className="promo-code">
            <h4>Phone Number</h4>
            <input
              className="select-dropdown"
              type="text"
              value={phone}
              onChange={(item) => {
                setPhone(item.target.value);
              }}
            />
            <small>
              <em style={{ color: "#ff2100" }}>
                Your number would be called to verify the order placement
              </em>
            </small>
          </div>
          <div className="final-cost">
            <h4>Total Cost</h4>
            <h4>
              {hasItems ? brl(store.state.cartTotal + deliveryCost) : brl(0)}
            </h4>
          </div>
          <div className="final-checkout">
            <button
              className="flat-button checkout"
              onClick={checkOut}
              disabled={!hasItems || processing}
            >
              {auth.state.user ? "Checkout" : "Login to checkout"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default OrderSummary;
