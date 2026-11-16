import React from "react";
import { useGlobalContext } from "../components/GlobalContext/GlobalContext";
import { useEffect, useState } from "react";
import DeliveryEmpty from "../components/Delivery/DeliveryEmpty/DeliveryEmpty";
import DeliveryItem from "../components/Delivery/DeliveryItem/DeliveryItem";
import Skeleton from "react-loading-skeleton";
import { toast } from "react-toastify";

const DeliveryView = () => {
  const { orders, auth, modal } = useGlobalContext();
  const [loading, setLoading] = useState(false);
  const [disabled, setDisabled] = useState(false);
  const userId = auth.state.user?.id;

  // sempre busca os pedidos ao abrir a página (ou quando o usuário muda)
  useEffect(() => {
    if (!userId) {
      modal.openModal(false);
      return;
    }
    setLoading(true);
    orders.getOrders(userId).finally(() => setLoading(false));
  }, [userId]);

  const reloadOrders = async () => {
    setDisabled(true);
    toast.info("Reloading orders...");
    await orders.getOrders(userId);
    setDisabled(false);
    toast.success("Orders reloaded!");
  };

  if (auth.state.user == null) {
    return <DeliveryEmpty></DeliveryEmpty>;
  }

  return (
    <div>
      <div className="reload-orders">
        <button
          className="btn-rounded"
          onClick={reloadOrders}
          disabled={disabled}
        >
          Reload Orders
        </button>
      </div>
      {loading ? (
        <Skeleton height={500}></Skeleton>
      ) : orders.state.orders.length > 0 ? (
        orders.state.orders.map((order) => (
          <DeliveryItem key={order._id} order={order}></DeliveryItem>
        ))
      ) : (
        <DeliveryEmpty></DeliveryEmpty>
      )}
    </div>
  );
};

export default DeliveryView;
