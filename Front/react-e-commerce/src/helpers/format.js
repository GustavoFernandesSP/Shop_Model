// Formata número como moeda brasileira: 19.9 -> "R$ 19,90"
export const brl = (value) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    Number(value) || 0
  );
