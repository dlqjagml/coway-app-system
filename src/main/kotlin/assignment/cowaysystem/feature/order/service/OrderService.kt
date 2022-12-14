package assignment.cowaysystem.feature.order.service

import assignment.cowaysystem.common.exception.BadRequestException
import assignment.cowaysystem.common.exception.NotFoundException
import assignment.cowaysystem.feature.order.controller.dto.OrderList
import assignment.cowaysystem.feature.order.controller.dto.OrderReq
import assignment.cowaysystem.feature.order.entity.*
import assignment.cowaysystem.feature.order.repository.ItemRepository
import assignment.cowaysystem.feature.order.repository.MemberRepository
import assignment.cowaysystem.feature.order.repository.OrderRepository
import assignment.cowaysystem.feature.order.entity.item.Item
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class OrderService(
        private val orderRepository: OrderRepository,
        private val itemRepository: ItemRepository,
        private val memberRepository: MemberRepository
){

    /**
     * 주문 기능
     * 1. 주문한 아이템이 리스트 형식으로 가능하게 하기 (o)
     * 2. 주문한 상품에 대해 주기적 방문 서비스 이용 여부 확인하는 코드 작성 필요 (o)
     * 3. 상품 주문시 color 일치하여 주문하는 코드 추가하기 (o)
     */
    @Transactional
    fun order(
            loginId: String,
            orderReq: List<OrderReq>
    ): Order{
        /**
         * 주문한 사용자 데이터 베이스에 있는지 확인
         */
        val orderMember = memberRepository.findByLoginId(loginId)
                ?: throw BadRequestException("주문하려는 ${loginId}가 존재하지 않습니다.")

        /**
         * 주문한 상품의 리스트
         */
        val orderList = orderReq.map {
            OrderList(
                    itemRepository.findByNameAndColor(it.itemName, it.color)
                            ?: throw BadRequestException("주문하려는 ${it.itemName}, ${it.color} 가 존재하지 않습니다."),
                    it.count,
                    it.color,
                    it.serviceYn
            )
        }

        /**
         * 배송정보 -> 회원의 정보로 기입
         */
        val delivery = Delivery().also {
            it.address = orderMember.address
            it.status = DeliveryStatus.READY
            it.deliveryStDt = LocalDateTime.now()
            it.deliveryEdDt = LocalDateTime.now()
        }

        /**
         * orderItems 생성 (아이템 수량 관리, 방문 서비스 이용여부)
         */
        val orderItems = orderList.map {
            OrderItem().createOrderItem(
                    it.item,
                    it.count,
                    it.color,
                    it.serviceYn
            )
        }

        /**
         * 주문 생성
         * 1. orderItems 바탕으로 주문생성
         * 2. member 바탕으로 주문 회원 확인
         * 3. member 주소를 바탕으로 delivery 생성
         */
        val createdOrder = Order().createOrder(
                member = orderMember,
                delivery = delivery,
                orderItems = orderItems
        )

        return orderRepository.save(createdOrder)
    }

    /**
     * 주문 취소
     */
    @Transactional
    fun cancelOrder(orderId: Long?): Boolean{
        val findOrder = orderRepository.findByIdOrNull(orderId)?: throw NotFoundException("${orderId}를 찾을수 없습니다.")
        findOrder.cancel()
        return true
    }

    /**
     * 특정 회원이 주문한 모든 내용을 보여줌
     */
    fun findOrders(loginId: String?): List<Order>{
        val orders = orderRepository.findOrderByLoginId(loginId, OrderStatus.ORDER)
        if (orders.isEmpty()) throw NotFoundException("${loginId}이 주문한 내용을 찾을 수 없습니다.")
        return orders
    }

    /**
     * 특정 회원이 주문한 취소한 모든 내용을 보여줌
     */
    fun findCancelOrders(loginId: String?): List<Order>{
        val orders = orderRepository.findOrderByLoginId(loginId, OrderStatus.CANCEL)
        if (orders.isEmpty()) throw NotFoundException("${loginId}이 주문취소한 내용을 찾을 수 없습니다.")
        return orders
    }
}